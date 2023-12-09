package com.github.maeda6uiui.mechtatel.core.vulkan.screen;

import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.nabor.FlexibleNaborInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.MergeScenesNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PrimitiveNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.gbuffer.GBufferNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNaborChain;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.shadow.ShadowMappingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import jakarta.validation.constraints.NotNull;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.awt.image.BufferedImage;
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
public class VkMttScreen implements IVkMttScreenForVkMttTexture, IVkMttScreenForVkMttComponent {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private GBufferNabor gBufferNabor;
    private PrimitiveNabor primitiveNabor;
    private PrimitiveNabor primitiveFillNabor;
    private MergeScenesNabor mergeScenesNabor;
    private MergeScenesNabor mergeScenesFillNabor;

    private static Map<String, List<Long>> vertShaderModulesStorage = new HashMap<>();
    private static Map<String, List<Long>> fragShaderModulesStorage = new HashMap<>();

    private PostProcessingNaborChain ppNaborChain;

    private ShadowMappingNabor shadowMappingNabor;
    private int depthImageFormat;
    private int depthImageWidth;
    private int depthImageHeight;
    private int depthImageAspect;

    private int initialWidth;
    private int initialHeight;
    private boolean shouldChangeExtentOnRecreate;

    private QuadDrawer quadDrawer;

    private void createShadowMappingNaborUserDefImages(ShadowMappingNabor shadowMappingNabor) {
        shadowMappingNabor.cleanupUserDefImages();

        //Shadow depth
        for (int i = 0; i < ShadowMappingNabor.MAX_NUM_SHADOW_MAPS; i++) {
            shadowMappingNabor.createUserDefImage(
                    depthImageWidth,
                    depthImageHeight,
                    1,
                    VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    depthImageFormat,
                    VK_IMAGE_ASPECT_DEPTH_BIT);
        }
    }

    public VkMttScreen(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            int depthImageFormat,
            int depthImageWidth,
            int depthImageHeight,
            int depthImageAspect,
            int colorImageFormat,
            int albedoMSAASamples,
            int samplerFilter,
            int samplerMipmapMode,
            int samplerAddressMode,
            VkExtent2D extent,
            boolean shouldChangeExtentOnRecreate,
            boolean useShadowMapping,
            @NotNull Map<String, FlexibleNaborInfo> flexibleNaborInfos,
            @NotNull List<String> ppNaborNames) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        this.depthImageFormat = depthImageFormat;
        this.depthImageWidth = depthImageWidth;
        this.depthImageHeight = depthImageHeight;
        this.depthImageAspect = depthImageAspect;

        initialWidth = extent.width();
        initialHeight = extent.height();
        this.shouldChangeExtentOnRecreate = shouldChangeExtentOnRecreate;

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);

        //GBuffer nabor
        gBufferNabor = new GBufferNabor(
                device,
                albedoMSAASamples,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT);
        if (vertShaderModulesStorage.containsKey("gbuffer")) {
            var vertShaderModules = vertShaderModulesStorage.get("gbuffer");
            var fragShaderModules = fragShaderModulesStorage.get("gbuffer");

            gBufferNabor.compile(
                    colorImageFormat,
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
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
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
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
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
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
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
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
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
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
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
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
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
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
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
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
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
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
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
                    extent,
                    commandPool,
                    graphicsQueue,
                    1);

            var vertShaderModules = mergeScenesFillNabor.getVertShaderModules();
            var fragShaderModules = mergeScenesFillNabor.getFragShaderModules();
            vertShaderModulesStorage.put("merge_scenes_fill", vertShaderModules);
            fragShaderModulesStorage.put("merge_scenes_fill", fragShaderModules);
        }

        if (!ppNaborNames.isEmpty()) {
            ppNaborChain = new PostProcessingNaborChain(
                    device,
                    commandPool,
                    graphicsQueue,
                    colorImageFormat,
                    samplerFilter,
                    samplerMipmapMode,
                    samplerAddressMode,
                    extent,
                    ppNaborNames,
                    flexibleNaborInfos,
                    new HashMap<>(vertShaderModulesStorage),
                    new HashMap<>(fragShaderModulesStorage)
            );

            var ppNaborChainVertShaderModules = ppNaborChain.getVertShaderModules();
            vertShaderModulesStorage.putAll(ppNaborChainVertShaderModules);

            var ppNaborChainFragShaderModules = ppNaborChain.getFragShaderModules();
            fragShaderModulesStorage.putAll(ppNaborChainFragShaderModules);
        }

        if (useShadowMapping) {
            shadowMappingNabor = new ShadowMappingNabor(device, depthImageFormat, depthImageWidth, depthImageHeight);
            if (vertShaderModulesStorage.containsKey("shadow_mapping")) {
                var vertShaderModules = vertShaderModulesStorage.get("shadow_mapping");
                var fragShaderModules = fragShaderModulesStorage.get("shadow_mapping");

                shadowMappingNabor.compile(
                        colorImageFormat,
                        samplerFilter,
                        samplerMipmapMode,
                        samplerAddressMode,
                        extent,
                        commandPool,
                        graphicsQueue,
                        1,
                        vertShaderModules,
                        fragShaderModules);
            } else {
                shadowMappingNabor.compile(
                        colorImageFormat,
                        samplerFilter,
                        samplerMipmapMode,
                        samplerAddressMode,
                        extent,
                        commandPool,
                        graphicsQueue,
                        1);

                var vertShaderModules = shadowMappingNabor.getVertShaderModules();
                var fragShaderModules = shadowMappingNabor.getFragShaderModules();
                vertShaderModulesStorage.put("shadow_mapping", vertShaderModules);
                fragShaderModulesStorage.put("shadow_mapping", fragShaderModules);
            }

            this.createShadowMappingNaborUserDefImages(shadowMappingNabor);
        }
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
            if (shadowMappingNabor != null) {
                shadowMappingNabor.recreate(colorImageFormat, extent);
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
            if (shadowMappingNabor != null) {
                shadowMappingNabor.recreate(colorImageFormat, initialExtent);
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
        if (shadowMappingNabor != null) {
            shadowMappingNabor.cleanup(false);
        }
    }

    public int getScreenWidth() {
        return gBufferNabor.getExtent(0).width();
    }

    public int getScreenHeight() {
        return gBufferNabor.getExtent(0).height();
    }

    public GBufferNabor getgBufferNabor() {
        return gBufferNabor;
    }

    private void runAlbedoNabor(
            VkCommandBuffer commandBuffer,
            Vector4f backgroundColor,
            Camera camera,
            List<VkMttComponent> components) {
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
                    if (component.getScreen() != this) {
                        continue;
                    }
                    if (!component.getNaborName().equals("gbuffer")) {
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
            List<VkMttComponent> components) {
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
                    if (component.getScreen() != this) {
                        continue;
                    }
                    if (!component.getNaborName().equals("gbuffer")) {
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
            List<VkMttComponent> components) {
        VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);
        this.runAlbedoNabor(commandBuffer, backgroundColor, camera, components);
        this.runPropertiesNabor(commandBuffer, camera, components);
        CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
    }

    private void runPrimitiveNabor(
            Vector4f backgroundColor,
            Camera camera,
            List<VkMttComponent> components) {
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
                    if (component.getNaborName().equals("primitive")) {
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
            List<VkMttComponent> components) {
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
                    if (component.getNaborName().equals("primitive_fill")) {
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

                gBufferNabor.transitionAlbedoImageLayout(commandPool, graphicsQueue);
                gBufferNabor.transitionDepthImageLayout(commandPool, graphicsQueue);
                gBufferNabor.transitionPositionImageLayout(commandPool, graphicsQueue);
                gBufferNabor.transitionNormalImageLayout(commandPool, graphicsQueue);

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
            SimpleBlurInfo simpleBlurInfo,
            List<VkMttComponent> components) {
        this.runGBufferNabor(backgroundColor, camera, components);
        this.runPrimitiveNabor(backgroundColor, camera, components);
        this.runPrimitiveFillNabor(backgroundColor, camera, components);
        this.runMergeScenesNabor();
        this.runMergeScenesFillNabor();

        mergeScenesFillNabor.transitionAlbedoImage(commandPool, graphicsQueue);
        mergeScenesFillNabor.transitionDepthImage(commandPool, graphicsQueue);
        mergeScenesFillNabor.transitionPositionImage(commandPool, graphicsQueue);
        mergeScenesFillNabor.transitionNormalImage(commandPool, graphicsQueue);

        if (shadowMappingNabor != null) {
            ShadowMappingNaborRunner.runShadowMappingNabor(
                    device,
                    commandPool,
                    graphicsQueue,
                    mergeScenesFillNabor,
                    null,
                    shadowMappingNabor,
                    parallelLights,
                    spotlights,
                    components,
                    depthImageAspect,
                    shadowMappingSettings,
                    quadDrawer
            );
        }

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
                    simpleBlurInfo,
                    mergeScenesFillNabor,
                    shadowMappingNabor
            );
        }
    }

    public long getColorImageView() {
        if (ppNaborChain != null) {
            return ppNaborChain.getLastPPNaborColorImageView();
        } else if (shadowMappingNabor != null) {
            return shadowMappingNabor.getColorImageView();
        } else {
            return mergeScenesFillNabor.getAlbedoImageView();
        }
    }

    public long getDepthImageView() {
        return mergeScenesFillNabor.getDepthImageView();
    }

    public VkMttTexture texturize(ScreenImageType imageType, VkMttScreen dstScreen) {
        long imageView;
        switch (imageType) {
            case COLOR -> imageView = this.getColorImageView();
            case DEPTH -> imageView = this.getDepthImageView();
            default -> throw new IllegalArgumentException("Unsupported image type specified: " + imageType);
        }

        return new VkMttTexture(device, dstScreen, imageView);
    }

    public BufferedImage createBufferedImage(int imageIndex, PixelFormat pixelFormat) {
        if (ppNaborChain != null) {
            return ppNaborChain.createBufferedImage(imageIndex, pixelFormat);
        } else if (shadowMappingNabor != null) {
            return shadowMappingNabor.createBufferedImage(commandPool, graphicsQueue, imageIndex, pixelFormat);
        } else {
            return mergeScenesFillNabor.createBufferedImage(commandPool, graphicsQueue, imageIndex, pixelFormat);
        }
    }

    @Override
    public void updateTextureDescriptorSets(int allocationIndex, long textureImageView) {
        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        int setCount = gBufferNabor.getSetCount(0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorImageInfo.Buffer textureInfo = VkDescriptorImageInfo.calloc(1, stack);
            textureInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            textureInfo.imageView(textureImageView);

            VkWriteDescriptorSet.Buffer textureDescriptorWrite = VkWriteDescriptorSet.calloc(1, stack);
            textureDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            textureDescriptorWrite.dstBinding(0);
            textureDescriptorWrite.dstArrayElement(allocationIndex);
            textureDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            textureDescriptorWrite.descriptorCount(1);
            textureDescriptorWrite.pImageInfo(textureInfo);

            for (int i = 0; i < descriptorSets.size(); i++) {
                //Update set 1
                if (i % setCount == 1) {
                    textureDescriptorWrite.dstSet(descriptorSets.get(i));
                    vkUpdateDescriptorSets(device, textureDescriptorWrite, null);
                }
            }
        }
    }

    @Override
    public void resetTextureDescriptorSets(int allocationIndex) {
        long dummyImageView = gBufferNabor.getDummyImageView();
        this.updateTextureDescriptorSets(allocationIndex, dummyImageView);
    }
}
