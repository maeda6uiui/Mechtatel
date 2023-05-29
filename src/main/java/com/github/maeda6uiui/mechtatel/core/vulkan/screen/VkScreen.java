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
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private String screenName;

    private GBufferNabor gBufferNabor;
    private PrimitiveNabor primitiveNabor;
    private PrimitiveNabor primitiveFillNabor;
    private MergeScenesNabor mergeScenesNabor;
    private MergeScenesNabor mergeScenesFillNabor;

    private static Map<String, List<Long>> vertShaderModulesStorage = new HashMap<>();
    private static Map<String, List<Long>> fragShaderModulesStorage = new HashMap<>();

    private PostProcessingNaborChain ppNaborChain;

    private int initialWidth;
    private int initialHeight;
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
            List<String> ppNaborNames,
            String screenName) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        this.screenName = screenName;

        //GBuffer nabor
        gBufferNabor = new GBufferNabor(
                device,
                albedoMsaaSamples,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        if (vertShaderModulesStorage.containsKey("gbuffer")) {
            var vertShaderModules = vertShaderModulesStorage.get("gbuffer");
            var fragShaderModules = fragShaderModulesStorage.get("gbuffer");

            gBufferNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1,
                    vertShaderModules,
                    fragShaderModules
            );
        } else {
            gBufferNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1);

            var vertShaderModules = gBufferNabor.getVertShaderModules();
            var fragShaderModules = gBufferNabor.getFragShaderModules();
            vertShaderModulesStorage.put("gbuffer", vertShaderModules);
            fragShaderModulesStorage.put("gbuffer", fragShaderModules);
        }

        //Primitive nabor
        primitiveNabor = new PrimitiveNabor(
                device,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                false);
        if (vertShaderModulesStorage.containsKey("primitive")) {
            var vertShaderModules = vertShaderModulesStorage.get("primitive");
            var fragShaderModules = fragShaderModulesStorage.get("primitive");

            primitiveNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1,
                    vertShaderModules,
                    fragShaderModules
            );
        } else {
            primitiveNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1);

            var vertShaderModules = primitiveNabor.getVertShaderModules();
            var fragShaderModules = primitiveNabor.getFragShaderModules();
            vertShaderModulesStorage.put("primitive", vertShaderModules);
            fragShaderModulesStorage.put("primitive", fragShaderModules);
        }

        //Primitive-Fill nabor
        primitiveFillNabor = new PrimitiveNabor(
                device,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                true);
        if (vertShaderModulesStorage.containsKey("primitive_fill")) {
            var vertShaderModules = vertShaderModulesStorage.get("primitive_fill");
            var fragShaderModules = fragShaderModulesStorage.get("primitive_fill");

            primitiveFillNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1,
                    vertShaderModules,
                    fragShaderModules
            );
        } else {
            primitiveFillNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1);

            var vertShaderModules = primitiveFillNabor.getVertShaderModules();
            var fragShaderModules = primitiveFillNabor.getFragShaderModules();
            vertShaderModulesStorage.put("primitive_fill", vertShaderModules);
            fragShaderModulesStorage.put("primitive_fill", fragShaderModules);
        }

        //Merge-Scenes nabor
        mergeScenesNabor = new MergeScenesNabor(
                device,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        if (vertShaderModulesStorage.containsKey("merge_scenes")) {
            var vertShaderModules = vertShaderModulesStorage.get("merge_scenes");
            var fragShaderModules = fragShaderModulesStorage.get("merge_scenes");

            mergeScenesNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1,
                    vertShaderModules,
                    fragShaderModules
            );
        } else {
            mergeScenesNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1);

            var vertShaderModules = mergeScenesNabor.getVertShaderModules();
            var fragShaderModules = mergeScenesNabor.getFragShaderModules();
            vertShaderModulesStorage.put("merge_scenes", vertShaderModules);
            fragShaderModulesStorage.put("merge_scenes", fragShaderModules);
        }

        //Merge-Scenes-Fill nabor
        mergeScenesFillNabor = new MergeScenesNabor(
                device,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        if (vertShaderModulesStorage.containsKey("merge_scenes_fill")) {
            var vertShaderModules = vertShaderModulesStorage.get("merge_scenes_fill");
            var fragShaderModules = fragShaderModulesStorage.get("merge_scenes_fill");

            mergeScenesFillNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1,
                    vertShaderModules,
                    fragShaderModules
            );
        } else {
            mergeScenesFillNabor.compile(
                    colorImageFormat,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1);

            var vertShaderModules = mergeScenesFillNabor.getVertShaderModules();
            var fragShaderModules = mergeScenesFillNabor.getFragShaderModules();
            vertShaderModulesStorage.put("merge_scenes_fill", vertShaderModules);
            fragShaderModulesStorage.put("merge_scenes_fill", fragShaderModules);
        }

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
                    ppNaborNames,
                    new HashMap<>(vertShaderModulesStorage),
                    new HashMap<>(fragShaderModulesStorage)
            );

            var ppNaborChainVertShaderModules = ppNaborChain.getVertShaderModules();
            ppNaborChainVertShaderModules.forEach(
                    (naborName, vertShaderModules) -> vertShaderModulesStorage.put(naborName, vertShaderModules)
            );

            var ppNaborChainFragShaderModules = ppNaborChain.getFragShaderModules();
            ppNaborChainFragShaderModules.forEach(
                    (naborName, fragShaderModules) -> fragShaderModulesStorage.put(naborName, fragShaderModules)
            );
        }

        initialWidth = extent.width();
        initialHeight = extent.height();
        this.shouldChangeExtentOnRecreate = shouldChangeExtentOnRecreate;

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);
    }

    public void recreate(int colorImageFormat, VkExtent2D extent) {
        if (shouldChangeExtentOnRecreate) {
            gBufferNabor.recreate(colorImageFormat, extent);
            primitiveNabor.recreate(colorImageFormat, extent);
            primitiveFillNabor.recreate(colorImageFormat, extent);
            mergeScenesNabor.recreate(colorImageFormat, extent);
            mergeScenesFillNabor.recreate(colorImageFormat, extent);

            if (ppNaborChain != null) {
                ppNaborChain.recreate(colorImageFormat, extent);
            }
        } else {
            VkExtent2D initialExtent = VkExtent2D.create();
            initialExtent.width(initialWidth);
            initialExtent.height(initialHeight);

            gBufferNabor.recreate(colorImageFormat, initialExtent);
            primitiveNabor.recreate(colorImageFormat, initialExtent);
            primitiveFillNabor.recreate(colorImageFormat, initialExtent);
            mergeScenesNabor.recreate(colorImageFormat, initialExtent);
            mergeScenesFillNabor.recreate(colorImageFormat, initialExtent);

            if (ppNaborChain != null) {
                ppNaborChain.recreate(colorImageFormat, initialExtent);
            }
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

    public String getScreenName() {
        return screenName;
    }

    public GBufferNabor getgBufferNabor() {
        return gBufferNabor;
    }

    public void updateTextureAllocations() {
        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }
        long dummyImageView = gBufferNabor.getDummyImageView();

        var invalidAllocations = VkTexture.getInvalidAllocations();
        for (var entry : invalidAllocations.entrySet()) {
            if (entry.getValue().equals(screenName)) {
                VkTexture.updateDescriptorSets(
                        device,
                        descriptorSets,
                        gBufferNabor.getSetCount(0),
                        entry.getKey(),
                        dummyImageView
                );
            }
        }

        VkTexture.clearInvalidAllocations(screenName);
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
                    if (component.getScreenName().equals(screenName) == false) {
                        continue;
                    }
                    if (component.getComponentType().equals("gbuffer") == false) {
                        continue;
                    }

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
                    if (component.getScreenName().equals(screenName) == false) {
                        continue;
                    }
                    if (component.getComponentType().equals("gbuffer") == false) {
                        continue;
                    }

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
                    if (component.getComponentType().equals("primitive")) {
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
                    if (component.getComponentType().equals("primitive_fill")) {
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

        mergeScenesFillNabor.transitionAlbedoImage(commandPool, graphicsQueue);
        mergeScenesFillNabor.transitionDepthImage(commandPool, graphicsQueue);
        mergeScenesFillNabor.transitionPositionImage(commandPool, graphicsQueue);
        mergeScenesFillNabor.transitionNormalImage(commandPool, graphicsQueue);

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

    public long getColorImageView() {
        if (ppNaborChain == null) {
            return mergeScenesFillNabor.getAlbedoImageView();
        } else {
            return ppNaborChain.getLastPPNaborColorImageView();
        }
    }

    public long getDepthImageView() {
        return mergeScenesFillNabor.getDepthImageView();
    }

    public List<Long> getDescriptorSets() {
        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        return descriptorSets;
    }

    public int getSetCount() {
        return gBufferNabor.getSetCount(0);
    }

    public void save(String srcImageFormat, String outputFilepath) throws IOException {
        if (ppNaborChain == null) {
            mergeScenesFillNabor.save(commandPool, graphicsQueue, 0, srcImageFormat, outputFilepath);
        } else {
            ppNaborChain.save(srcImageFormat, outputFilepath);
        }
    }
}
