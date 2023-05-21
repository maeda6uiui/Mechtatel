package com.github.maeda6uiui.mechtatel.core.vulkan.screen;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.MergeScenesNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PrimitiveNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.gbuffer.GBufferNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNaborChain;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Screen
 *
 * @author maeda6uiui
 */
public class VkScreen {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private GBufferNabor gBufferNabor;
    private PrimitiveNabor primitiveNabor;
    private PrimitiveNabor primitiveFillNabor;
    private MergeScenesNabor mergeScenesNabor;
    private MergeScenesNabor mergeScenesFillNabor;

    private PostProcessingNaborChain ppNaborChain;

    private VkExtent2D initialExtent;
    private boolean shouldChangeExtentOnRecreate;

    private QuadDrawer quadDrawer;

    public VkScreen(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            int depthImageFormat,
            int depthImageWidth,
            int depthImageHeight,
            int depthImageAspect,
            int colorImageFormat,
            int albedoMsaaSamples,
            VkExtent2D extent,
            boolean shouldChangeExtentOnRecreate,
            List<String> ppNaborNames) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        gBufferNabor = new GBufferNabor(
                device,
                albedoMsaaSamples,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        gBufferNabor.compile(
                colorImageFormat,
                extent,
                commandPool,
                graphicsQueue,
                1);

        primitiveNabor = new PrimitiveNabor(
                device,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                false);
        primitiveNabor.compile(
                colorImageFormat,
                extent,
                commandPool,
                graphicsQueue,
                1);
        primitiveFillNabor = new PrimitiveNabor(
                device,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                true);
        primitiveFillNabor.compile(
                colorImageFormat,
                extent,
                commandPool,
                graphicsQueue,
                1);

        mergeScenesNabor = new MergeScenesNabor(
                device,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        mergeScenesNabor.compile(
                colorImageFormat,
                extent,
                commandPool,
                graphicsQueue,
                1);
        mergeScenesFillNabor = new MergeScenesNabor(
                device,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        mergeScenesFillNabor.compile(
                colorImageFormat,
                extent,
                commandPool,
                graphicsQueue,
                1);

        if (ppNaborNames != null) {
            ppNaborChain = new PostProcessingNaborChain(
                    device,
                    commandPool,
                    graphicsQueue,
                    depthImageFormat,
                    depthImageWidth,
                    depthImageHeight,
                    depthImageAspect,
                    colorImageFormat,
                    extent,
                    ppNaborNames
            );
        }

        initialExtent = extent;
        this.shouldChangeExtentOnRecreate = shouldChangeExtentOnRecreate;

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);
    }

    public void recreate(int colorImageFormat, VkExtent2D extent) {
        if (!shouldChangeExtentOnRecreate) {
            extent = initialExtent;
        }

        gBufferNabor.recreate(colorImageFormat, extent);
        primitiveNabor.recreate(colorImageFormat, extent);
        primitiveFillNabor.recreate(colorImageFormat, extent);
        mergeScenesNabor.recreate(colorImageFormat, extent);
        mergeScenesFillNabor.recreate(colorImageFormat, extent);

        if (ppNaborChain != null) {
            ppNaborChain.recreate(colorImageFormat, extent);
        }
    }

    public void cleanup() {
        quadDrawer.cleanup();

        gBufferNabor.cleanup(false);
        primitiveNabor.cleanup(false);
        primitiveFillNabor.cleanup(false);
        mergeScenesNabor.cleanup(false);
        mergeScenesFillNabor.cleanup(false);

        if (ppNaborChain != null) {
            ppNaborChain.cleanup();
        }
    }

    public GBufferNabor getgBufferNabor() {
        return gBufferNabor;
    }

    private void runAlbedoNabor(
            VkCommandBuffer commandBuffer,
            Vector4f backgroundColor,
            Camera camera,
            List<VkComponent> components) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long cameraUBOMemory = gBufferNabor.getUniformBufferMemory(0, 0);
            var cameraUBO = new CameraUBO(camera);
            cameraUBO.update(device, cameraUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(gBufferNabor.getRenderPass(0));
            renderPassInfo.framebuffer(gBufferNabor.getFramebuffer(0, 0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(gBufferNabor.getExtent(0));
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(2, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(
                    stack.floats(
                            backgroundColor.x,
                            backgroundColor.y,
                            backgroundColor.z,
                            backgroundColor.w));
            renderPassInfo.pClearValues(clearValues);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, gBufferNabor.getGraphicsPipeline(0, 0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        gBufferNabor.getPipelineLayout(0, 0),
                        0,
                        gBufferNabor.pDescriptorSets(0),
                        null);

                for (var component : components) {
                    if (component.getComponentType() == "gbuffer") {
                        ByteBuffer pcBuffer = stack.calloc(1 * 16 * Float.BYTES + 1 * Integer.BYTES);
                        component.getMat().get(pcBuffer);
                        if (component.isTwoDComponent()) {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 1);
                        } else {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 0);
                        }

                        vkCmdPushConstants(
                                commandBuffer,
                                gBufferNabor.getPipelineLayout(0, 0),
                                VK_SHADER_STAGE_VERTEX_BIT,
                                0,
                                pcBuffer);

                        component.draw(commandBuffer, gBufferNabor.getPipelineLayout(0, 0));
                    }
                }
            }
            vkCmdEndRenderPass(commandBuffer);
        }
    }

    private void runPropertiesNabor(
            VkCommandBuffer commandBuffer,
            Camera camera,
            List<VkComponent> components) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long cameraUBOMemory = gBufferNabor.getUniformBufferMemory(1, 0);
            var cameraUBO = new CameraUBO(camera);
            cameraUBO.update(device, cameraUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(gBufferNabor.getRenderPass(1));
            renderPassInfo.framebuffer(gBufferNabor.getFramebuffer(1, 0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(gBufferNabor.getExtent(1));
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(3, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, gBufferNabor.getGraphicsPipeline(1, 0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        gBufferNabor.getPipelineLayout(1, 0),
                        0,
                        gBufferNabor.pDescriptorSets(1),
                        null);

                for (var component : components) {
                    if (component.getComponentType() == "gbuffer") {
                        ByteBuffer pcBuffer = stack.calloc(1 * 16 * Float.BYTES + 1 * Integer.BYTES);
                        component.getMat().get(pcBuffer);
                        if (component.isTwoDComponent()) {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 1);
                        } else {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 0);
                        }

                        vkCmdPushConstants(
                                commandBuffer,
                                gBufferNabor.getPipelineLayout(1, 0),
                                VK_SHADER_STAGE_VERTEX_BIT,
                                0,
                                pcBuffer);

                        component.transfer(commandBuffer);
                    }
                }
            }
            vkCmdEndRenderPass(commandBuffer);
        }
    }

    private void runGBufferNabor(
            Vector4f backgroundColor,
            Camera camera,
            List<VkComponent> components) {
        VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);
        this.runAlbedoNabor(commandBuffer, backgroundColor, camera, components);
        this.runPropertiesNabor(commandBuffer, camera, components);
        CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
    }

    private void runPrimitiveNabor(
            Vector4f backgroundColor,
            Camera camera,
            List<VkComponent> components) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long cameraUBOMemory = primitiveNabor.getUniformBufferMemory(0);
            var cameraUBO = new CameraUBO(camera);
            cameraUBO.update(device, cameraUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(primitiveNabor.getRenderPass());
            renderPassInfo.framebuffer(primitiveNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(primitiveNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(4, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(
                    stack.floats(
                            backgroundColor.x,
                            backgroundColor.y,
                            backgroundColor.z,
                            backgroundColor.w));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, primitiveNabor.getGraphicsPipeline(0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        primitiveNabor.getPipelineLayout(0),
                        0,
                        primitiveNabor.pDescriptorSets(),
                        null);

                for (var component : components) {
                    if (component.getComponentType() == "primitive") {
                        ByteBuffer pcBuffer = stack.calloc(1 * 16 * Float.BYTES + 1 * 1 * Integer.BYTES);
                        component.getMat().get(pcBuffer);
                        if (component.isTwoDComponent()) {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 1);
                        } else {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 0);
                        }

                        vkCmdPushConstants(
                                commandBuffer,
                                primitiveNabor.getPipelineLayout(0),
                                VK_SHADER_STAGE_VERTEX_BIT,
                                0,
                                pcBuffer);

                        component.draw(commandBuffer, primitiveNabor.getPipelineLayout(0));
                    }
                }
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void runPrimitiveFillNabor(
            Vector4f backgroundColor,
            Camera camera,
            List<VkComponent> components) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long cameraUBOMemory = primitiveFillNabor.getUniformBufferMemory(0);
            var cameraUBO = new CameraUBO(camera);
            cameraUBO.update(device, cameraUBOMemory);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(primitiveFillNabor.getRenderPass());
            renderPassInfo.framebuffer(primitiveFillNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(primitiveFillNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(4, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(
                    stack.floats(
                            backgroundColor.x,
                            backgroundColor.y,
                            backgroundColor.z,
                            backgroundColor.w));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, primitiveFillNabor.getGraphicsPipeline(0));

                vkCmdBindDescriptorSets(
                        commandBuffer,
                        VK_PIPELINE_BIND_POINT_GRAPHICS,
                        primitiveFillNabor.getPipelineLayout(0),
                        0,
                        primitiveFillNabor.pDescriptorSets(),
                        null);

                for (var component : components) {
                    if (component.getComponentType() == "primitive_fill") {
                        ByteBuffer pcBuffer = stack.calloc(1 * 16 * Float.BYTES + 1 * 1 * Integer.BYTES);
                        component.getMat().get(pcBuffer);
                        if (component.isTwoDComponent()) {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 1);
                        } else {
                            pcBuffer.putInt(1 * 16 * Float.BYTES, 0);
                        }

                        vkCmdPushConstants(
                                commandBuffer,
                                primitiveFillNabor.getPipelineLayout(0),
                                VK_SHADER_STAGE_VERTEX_BIT,
                                0,
                                pcBuffer);

                        component.draw(commandBuffer, primitiveFillNabor.getPipelineLayout(0));
                    }
                }
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void runMergeScenesNabor() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(mergeScenesNabor.getRenderPass());
            renderPassInfo.framebuffer(mergeScenesNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(mergeScenesNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(4, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(1).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, mergeScenesNabor.getGraphicsPipeline(0));

                gBufferNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                gBufferNabor.transitionDepthImage(commandPool, graphicsQueue);
                gBufferNabor.transitionPositionImage(commandPool, graphicsQueue);
                gBufferNabor.transitionNormalImage(commandPool, graphicsQueue);

                primitiveNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                primitiveNabor.transitionDepthImage(commandPool, graphicsQueue);
                primitiveNabor.transitionPositionImage(commandPool, graphicsQueue);
                primitiveNabor.transitionNormalImage(commandPool, graphicsQueue);

                mergeScenesNabor.bindAlbedoImages(commandBuffer, gBufferNabor.getAlbedoImageView(), primitiveNabor.getAlbedoImageView());
                mergeScenesNabor.bindDepthImages(commandBuffer, gBufferNabor.getDepthImageView(), primitiveNabor.getDepthImageView());
                mergeScenesNabor.bindPositionImages(commandBuffer, gBufferNabor.getPositionImageView(), primitiveNabor.getPositionImageView());
                mergeScenesNabor.bindNormalImages(commandBuffer, gBufferNabor.getNormalImageView(), primitiveNabor.getNormalImageView());

                quadDrawer.draw(commandBuffer);
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void runMergeScenesFillNabor() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(mergeScenesFillNabor.getRenderPass());
            renderPassInfo.framebuffer(mergeScenesFillNabor.getFramebuffer(0));
            VkRect2D renderArea = VkRect2D.calloc(stack);
            renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
            renderArea.extent(mergeScenesFillNabor.getExtent());
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.calloc(4, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(1).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, mergeScenesFillNabor.getGraphicsPipeline(0));

                mergeScenesNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                mergeScenesNabor.transitionDepthImage(commandPool, graphicsQueue);
                mergeScenesNabor.transitionPositionImage(commandPool, graphicsQueue);
                mergeScenesNabor.transitionNormalImage(commandPool, graphicsQueue);

                primitiveFillNabor.transitionAlbedoImage(commandPool, graphicsQueue);
                primitiveFillNabor.transitionDepthImage(commandPool, graphicsQueue);
                primitiveFillNabor.transitionPositionImage(commandPool, graphicsQueue);
                primitiveFillNabor.transitionNormalImage(commandPool, graphicsQueue);

                mergeScenesFillNabor.bindAlbedoImages(commandBuffer, mergeScenesNabor.getAlbedoImageView(), primitiveFillNabor.getAlbedoImageView());
                mergeScenesFillNabor.bindDepthImages(commandBuffer, mergeScenesNabor.getDepthImageView(), primitiveFillNabor.getDepthImageView());
                mergeScenesFillNabor.bindPositionImages(commandBuffer, mergeScenesNabor.getPositionImageView(), primitiveFillNabor.getPositionImageView());
                mergeScenesFillNabor.bindNormalImages(commandBuffer, mergeScenesNabor.getNormalImageView(), primitiveFillNabor.getNormalImageView());

                quadDrawer.draw(commandBuffer);
            }
            vkCmdEndRenderPass(commandBuffer);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    public void run(
            Vector4f backgroundColor,
            Camera camera,
            Fog fog,
            List<ParallelLight> parallelLights,
            Vector3f parallelLightAmbientColor,
            List<PointLight> pointLights,
            Vector3f pointLightAmbientColor,
            List<Spotlight> spotlights,
            Vector3f spotlightAmbientColor,
            ShadowMappingSettings shadowMappingSettings,
            List<VkComponent> components) {
        this.runGBufferNabor(backgroundColor, camera, components);
        this.runPrimitiveNabor(backgroundColor, camera, components);
        this.runPrimitiveFillNabor(backgroundColor, camera, components);
        this.runMergeScenesNabor();
        this.runMergeScenesFillNabor();

        if (ppNaborChain != null) {
            ppNaborChain.run(
                    camera,
                    fog,
                    parallelLights,
                    parallelLightAmbientColor,
                    pointLights,
                    pointLightAmbientColor,
                    spotlights,
                    spotlightAmbientColor,
                    shadowMappingSettings,
                    mergeScenesFillNabor,
                    components
            );
        }
    }

    public void transitionColorImageLayout() {
        if (ppNaborChain == null) {
            mergeScenesFillNabor.transitionAlbedoImage(commandPool, graphicsQueue);
        } else {
            ppNaborChain.transitionLastPPNaborColorImage();
        }
    }

    public long getColorImageView() {
        if (ppNaborChain == null) {
            return mergeScenesFillNabor.getAlbedoImageView();
        } else {
            return ppNaborChain.getLastPPNaborColorImageView();
        }
    }

    public void save(String srcImageFormat, String outputFilepath) throws IOException {
        if (ppNaborChain == null) {
            mergeScenesFillNabor.save(commandPool, graphicsQueue, 0, srcImageFormat, outputFilepath);
        } else {
            ppNaborChain.save(srcImageFormat, outputFilepath);
        }
    }
}
