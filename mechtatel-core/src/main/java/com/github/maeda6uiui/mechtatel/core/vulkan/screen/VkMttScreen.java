package com.github.maeda6uiui.mechtatel.core.vulkan.screen;

import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.nabor.MttShaderSettings;
import com.github.maeda6uiui.mechtatel.core.postprocessing.CustomizablePostProcessingNaborInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.util.MttURLUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.MergeScenesNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PrimitiveNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.gbuffer.GBufferNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNaborChain;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.shadow.ShadowMappingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.MergeScenesInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.*;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Vulkan implementation of screen
 *
 * @author maeda6uiui
 */
public class VkMttScreen implements IVkMttScreenForVkMttTexture, IVkMttScreenForVkMttComponent {
    //Number of scenes to merge to create an interim image
    //before proceeding to post-processing procedures
    //Currently, scenes from the following three nabors are merged:
    // - GBufferNabor
    // - PrimitiveNabor
    // - PrimitiveNabor (fill)
    private static final int NUM_SCENES_TO_MERGE = 3;

    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private GBufferNabor gBufferNabor;
    private PrimitiveNabor primitiveNabor;
    private PrimitiveNabor primitiveFillNabor;
    private MergeScenesNabor mergeScenesNabor;

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
            Map<String, CustomizablePostProcessingNaborInfo> customizableNaborInfos,
            List<String> ppNaborNames) {
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

        //Get shader URLs =====
        MttShaderSettings shaderSettings = MttShaderSettings
                .get()
                .orElse(MttShaderSettings.create());

        URL gBufferAlbedoVertShaderResource;
        URL gBufferAlbedoFragShaderResource;
        URL gBufferPropertiesVertShaderResource;
        URL gBufferPropertiesFragShaderResource;
        URL primitiveVertShaderResource;
        URL primitiveFragShaderResource;
        URL mergeScenesVertShaderResource;
        URL mergeScenesFragShaderResource;
        URL shadowMappingPass1VertShaderResource;
        URL shadowMappingPass1FragShaderResource;
        URL shadowMappingPass2VertShaderResource;
        URL shadowMappingPass2FragShaderResource;
        try {
            gBufferAlbedoVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.gBuffer.albedo.vert.filepath,
                    shaderSettings.gBuffer.albedo.vert.external
            );
            gBufferAlbedoFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.gBuffer.albedo.frag.filepath,
                    shaderSettings.gBuffer.albedo.frag.external
            );
            gBufferPropertiesVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.gBuffer.properties.vert.filepath,
                    shaderSettings.gBuffer.properties.vert.external
            );
            gBufferPropertiesFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.gBuffer.properties.frag.filepath,
                    shaderSettings.gBuffer.properties.frag.external
            );

            primitiveVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.primitive.main.vert.filepath,
                    shaderSettings.primitive.main.vert.external
            );
            primitiveFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.primitive.main.frag.filepath,
                    shaderSettings.primitive.main.frag.external
            );

            mergeScenesVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.mergeScenes.main.vert.filepath,
                    shaderSettings.mergeScenes.main.vert.external
            );
            mergeScenesFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.mergeScenes.main.frag.filepath,
                    shaderSettings.mergeScenes.main.frag.external
            );

            shadowMappingPass1VertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.shadowMapping.pass1.vert.filepath,
                    shaderSettings.shadowMapping.pass1.vert.external
            );
            shadowMappingPass1FragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.shadowMapping.pass1.frag.filepath,
                    shaderSettings.shadowMapping.pass1.frag.external
            );
            shadowMappingPass2VertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.shadowMapping.pass2.vert.filepath,
                    shaderSettings.shadowMapping.pass2.vert.external
            );
            shadowMappingPass2FragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.shadowMapping.pass2.frag.filepath,
                    shaderSettings.shadowMapping.pass2.frag.external
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        //==========

        //GBuffer nabor
        gBufferNabor = new GBufferNabor(
                device,
                albedoMSAASamples,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16_SFLOAT,
                gBufferAlbedoVertShaderResource,
                gBufferAlbedoFragShaderResource,
                gBufferPropertiesVertShaderResource,
                gBufferPropertiesFragShaderResource
        );
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
                VK_FORMAT_R16_SFLOAT,
                false,
                primitiveVertShaderResource,
                primitiveFragShaderResource
        );
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
                VK_FORMAT_R16_SFLOAT,
                true,
                primitiveVertShaderResource,
                primitiveFragShaderResource
        );
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
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                mergeScenesVertShaderResource,
                mergeScenesFragShaderResource
        );
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
                    customizableNaborInfos,
                    new HashMap<>(vertShaderModulesStorage),
                    new HashMap<>(fragShaderModulesStorage)
            );

            var ppNaborChainVertShaderModules = ppNaborChain.getVertShaderModules();
            vertShaderModulesStorage.putAll(ppNaborChainVertShaderModules);

            var ppNaborChainFragShaderModules = ppNaborChain.getFragShaderModules();
            fragShaderModulesStorage.putAll(ppNaborChainFragShaderModules);
        }

        if (useShadowMapping) {
            shadowMappingNabor = new ShadowMappingNabor(
                    device,
                    depthImageFormat,
                    depthImageWidth,
                    depthImageHeight,
                    shadowMappingPass1VertShaderResource,
                    shadowMappingPass1FragShaderResource,
                    shadowMappingPass2VertShaderResource,
                    shadowMappingPass2FragShaderResource
            );
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

                    String naborName = component.getNaborName();
                    if (naborName.equals("gbuffer")) {
                        //Bind first graphics pipeline of the albedo nabor
                        //First graphics pipeline has been set up for normal components
                        vkCmdBindPipeline(
                                commandBuffer,
                                VK_PIPELINE_BIND_POINT_GRAPHICS,
                                gBufferNabor.getGraphicsPipeline(0, 0)
                        );
                    } else if (naborName.equals("gbuffer_imgui")) {
                        //Bind second graphics pipeline of the albedo nabor
                        //Second graphics pipeline has been set up for ImGui components
                        vkCmdBindPipeline(
                                commandBuffer,
                                VK_PIPELINE_BIND_POINT_GRAPHICS,
                                gBufferNabor.getGraphicsPipeline(0, 1)
                        );
                    } else {
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

                    Map<String, Long> uniformBufferMemories = Map.of(
                            "animation",
                            gBufferNabor.getUniformBufferMemory(0, 1)
                    );
                    component.updateUBOs(device, uniformBufferMemories);

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

            VkClearValue.Buffer clearValues = VkClearValue.calloc(4, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(1.0f));
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

                    Map<String, Long> uniformBufferMemories = Map.of(
                            "animation",
                            gBufferNabor.getUniformBufferMemory(1, 1)
                    );
                    component.updateUBOs(device, uniformBufferMemories);

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

            VkClearValue.Buffer clearValues = VkClearValue.calloc(5, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(
                    stack.floats(
                            backgroundColor.x,
                            backgroundColor.y,
                            backgroundColor.z,
                            backgroundColor.w));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(4).color().float32(stack.floats(1.0f));
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
                    if (component.getScreen() != this) {
                        continue;
                    }
                    if (!component.getNaborName().equals("primitive")) {
                        continue;
                    }

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

            VkClearValue.Buffer clearValues = VkClearValue.calloc(5, stack);
            clearValues.get(0).depthStencil().set(1.0f, 0);
            clearValues.get(1).color().float32(
                    stack.floats(
                            backgroundColor.x,
                            backgroundColor.y,
                            backgroundColor.z,
                            backgroundColor.w));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(4).color().float32(stack.floats(1.0f));
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
                    if (component.getScreen() != this) {
                        continue;
                    }
                    if (!component.getNaborName().equals("primitive_fill")) {
                        continue;
                    }

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

            VkClearValue.Buffer clearValues = VkClearValue.calloc(5, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(1).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(2).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(3).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            clearValues.get(4).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));
            renderPassInfo.pClearValues(clearValues);

            long mergeScenesInfoUBOMemory = mergeScenesNabor.getUniformBufferMemory(0);
            var mergeScenesInfoUBO = new MergeScenesInfoUBO(NUM_SCENES_TO_MERGE);
            mergeScenesInfoUBO.update(device, mergeScenesInfoUBOMemory);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
            {
                vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, mergeScenesNabor.getGraphicsPipeline(0));

                gBufferNabor.transitionAlbedoImageLayout(commandPool, graphicsQueue);
                gBufferNabor.transitionDepthImageLayout(commandPool, graphicsQueue);
                gBufferNabor.transitionPositionImageLayout(commandPool, graphicsQueue);
                gBufferNabor.transitionNormalImageLayout(commandPool, graphicsQueue);
                gBufferNabor.transitionStencilImageLayout(commandPool, graphicsQueue);

                primitiveNabor.transitionAlbedoImageLayout(commandPool, graphicsQueue);
                primitiveNabor.transitionDepthImageLayout(commandPool, graphicsQueue);
                primitiveNabor.transitionPositionImageLayout(commandPool, graphicsQueue);
                primitiveNabor.transitionNormalImageLayout(commandPool, graphicsQueue);
                primitiveNabor.transitionStencilImageLayout(commandPool, graphicsQueue);

                primitiveFillNabor.transitionAlbedoImageLayout(commandPool, graphicsQueue);
                primitiveFillNabor.transitionDepthImageLayout(commandPool, graphicsQueue);
                primitiveFillNabor.transitionPositionImageLayout(commandPool, graphicsQueue);
                primitiveFillNabor.transitionNormalImageLayout(commandPool, graphicsQueue);
                primitiveFillNabor.transitionStencilImageLayout(commandPool, graphicsQueue);

                mergeScenesNabor.bindAlbedoImages(
                        commandBuffer,
                        Arrays.asList(
                                gBufferNabor.getAlbedoImageView(),
                                primitiveNabor.getAlbedoImageView(),
                                primitiveFillNabor.getAlbedoImageView()
                        )
                );
                mergeScenesNabor.bindDepthImages(
                        commandBuffer,
                        Arrays.asList(
                                gBufferNabor.getDepthImageView(),
                                primitiveNabor.getDepthImageView(),
                                primitiveFillNabor.getDepthImageView()
                        )
                );
                mergeScenesNabor.bindPositionImages(
                        commandBuffer,
                        Arrays.asList(
                                gBufferNabor.getPositionImageView(),
                                primitiveNabor.getPositionImageView(),
                                primitiveFillNabor.getPositionImageView()
                        )
                );
                mergeScenesNabor.bindNormalImages(
                        commandBuffer,
                        Arrays.asList(
                                gBufferNabor.getNormalImageView(),
                                primitiveNabor.getNormalImageView(),
                                primitiveFillNabor.getNormalImageView()
                        )
                );
                mergeScenesNabor.bindStencilImages(
                        commandBuffer,
                        Arrays.asList(
                                gBufferNabor.getStencilImageView(),
                                primitiveNabor.getStencilImageView(),
                                primitiveFillNabor.getStencilImageView()
                        )
                );

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

        mergeScenesNabor.transitionAlbedoImageLayout(commandPool, graphicsQueue);
        mergeScenesNabor.transitionDepthImageLayout(commandPool, graphicsQueue);
        mergeScenesNabor.transitionPositionImageLayout(commandPool, graphicsQueue);
        mergeScenesNabor.transitionNormalImageLayout(commandPool, graphicsQueue);
        mergeScenesNabor.transitionStencilImageLayout(commandPool, graphicsQueue);

        if (shadowMappingNabor != null) {
            ShadowMappingNaborRunner.runShadowMappingNabor(
                    device,
                    commandPool,
                    graphicsQueue,
                    mergeScenesNabor,
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
                    mergeScenesNabor,
                    shadowMappingNabor
            );
        }

        components.forEach(VkMttComponent::cleanupLocally);
    }

    public long getColorImageView() {
        if (ppNaborChain != null) {
            return ppNaborChain.getLastPPNaborColorImageView();
        } else if (shadowMappingNabor != null) {
            return shadowMappingNabor.getColorImageView();
        } else {
            return mergeScenesNabor.getAlbedoImageView();
        }
    }

    public long getDepthImageView() {
        return mergeScenesNabor.getDepthImageView();
    }

    public long getStencilImageView() {
        return mergeScenesNabor.getStencilImageView();
    }

    public VkMttTexture texturize(ScreenImageType imageType, VkMttScreen dstScreen) {
        long imageView = switch (imageType) {
            case COLOR -> this.getColorImageView();
            case DEPTH -> this.getDepthImageView();
            case STENCIL -> this.getStencilImageView();
        };

        return new VkMttTexture(device, dstScreen, imageView);
    }

    public BufferedImage createBufferedImage(ScreenImageType imageType, PixelFormat pixelFormat) {
        //Color image is acquired from one of the following sources:
        //- Post-processing nabor
        //- Shadow mapping nabor
        //- Merge-scenes nabor
        if (imageType == ScreenImageType.COLOR) {
            //Acquire image from a post-processing nabor
            if (ppNaborChain != null) {
                return ppNaborChain.createBufferedImage(PostProcessingNabor.COLOR_ATTACHMENT_INDEX, pixelFormat);
            }
            //Acquire image from shadow mapping nabor
            else if (shadowMappingNabor != null) {
                return shadowMappingNabor.createBufferedImage(
                        commandPool,
                        graphicsQueue,
                        ShadowMappingNabor.COLOR_ATTACHMENT_INDEX,
                        pixelFormat
                );
            }
            //Acquire image from merge-scenes nabor
            //if there is neither post-processing nor shadow mapping declared in this screen
            else {
                return mergeScenesNabor.createBufferedImage(
                        commandPool,
                        graphicsQueue,
                        MergeScenesNabor.ALBEDO_ATTACHMENT_INDEX,
                        pixelFormat
                );
            }
        }
        //Other type of image is acquired from merge-scenes nabor
        else {
            int imageIndex = switch (imageType) {
                case DEPTH -> MergeScenesNabor.DEPTH_ATTACHMENT_INDEX;
                case STENCIL -> MergeScenesNabor.STENCIL_ATTACHMENT_INDEX;
                default -> throw new RuntimeException("Unexpected image type: " + imageType);
            };

            return mergeScenesNabor.createBufferedImage(commandPool, graphicsQueue, imageIndex, pixelFormat);
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

    public List<Long> getTextureDescriptorSets() {
        int numDescriptorSets = gBufferNabor.getNumDescriptorSets(0);
        var descriptorSets = new ArrayList<Long>();
        for (int i = 0; i < numDescriptorSets; i++) {
            descriptorSets.add(gBufferNabor.getDescriptorSet(0, i));
        }

        int setCount = gBufferNabor.getSetCount(0);

        var textureDescriptorSets = new ArrayList<Long>();
        for (int i = 0; i < descriptorSets.size(); i++) {
            //Get set 1
            if (i % setCount == 1) {
                textureDescriptorSets.add(descriptorSets.get(i));
            }
        }

        return textureDescriptorSets;
    }
}
