package com.github.maeda6uiui.mechtatel.core.vulkan.screen;

import com.github.maeda6uiui.mechtatel.core.MttShaderSettings;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectNaborInfo;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectProperties;
import com.github.maeda6uiui.mechtatel.core.postprocessing.CustomizablePostProcessingNaborInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.util.MttURLUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.MergeScenesNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.Nabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.PrimitiveNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.fseffect.FullScreenEffectNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.fseffect.FullScreenEffectNaborChain;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.gbuffer.GBufferNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PostProcessingNaborChain;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.shadow.ShadowMappingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.MergeScenesInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Vulkan implementation of screen
 *
 * @author maeda6uiui
 */
public class VkMttScreen implements IVkMttScreenForVkMttTexture, IVkMttScreenForVkMttComponent {
    public static class VkMttScreenCreateInfo {
        public VkDevice device;
        public long commandPool;
        public VkQueue graphicsQueue;
        public int depthImageFormat;
        public int depthImageWidth;
        public int depthImageHeight;
        public int depthImageAspect;
        public int colorImageFormat;
        public int albedoMSAASamples;
        public int samplerFilter;
        public int samplerMipmapMode;
        public int samplerAddressMode;
        public VkExtent2D extent;
        public boolean shouldChangeExtentOnRecreate;
        public boolean useShadowMapping;
        public List<String> ppNaborNames;
        public Map<String, CustomizablePostProcessingNaborInfo> customizablePPNaborInfos;
        public List<String> fseNaborNames;
        public Map<String, FullScreenEffectNaborInfo> fseNaborInfos;

        public VkMttScreenCreateInfo setDevice(VkDevice device) {
            this.device = device;
            return this;
        }

        public VkMttScreenCreateInfo setCommandPool(long commandPool) {
            this.commandPool = commandPool;
            return this;
        }

        public VkMttScreenCreateInfo setGraphicsQueue(VkQueue graphicsQueue) {
            this.graphicsQueue = graphicsQueue;
            return this;
        }

        public VkMttScreenCreateInfo setDepthImageFormat(int depthImageFormat) {
            this.depthImageFormat = depthImageFormat;
            return this;
        }

        public VkMttScreenCreateInfo setDepthImageWidth(int depthImageWidth) {
            this.depthImageWidth = depthImageWidth;
            return this;
        }

        public VkMttScreenCreateInfo setDepthImageHeight(int depthImageHeight) {
            this.depthImageHeight = depthImageHeight;
            return this;
        }

        public VkMttScreenCreateInfo setDepthImageAspect(int depthImageAspect) {
            this.depthImageAspect = depthImageAspect;
            return this;
        }

        public VkMttScreenCreateInfo setColorImageFormat(int colorImageFormat) {
            this.colorImageFormat = colorImageFormat;
            return this;
        }

        public VkMttScreenCreateInfo setAlbedoMSAASamples(int albedoMSAASamples) {
            this.albedoMSAASamples = albedoMSAASamples;
            return this;
        }

        public VkMttScreenCreateInfo setSamplerFilter(int samplerFilter) {
            this.samplerFilter = samplerFilter;
            return this;
        }

        public VkMttScreenCreateInfo setSamplerMipmapMode(int samplerMipmapMode) {
            this.samplerMipmapMode = samplerMipmapMode;
            return this;
        }

        public VkMttScreenCreateInfo setSamplerAddressMode(int samplerAddressMode) {
            this.samplerAddressMode = samplerAddressMode;
            return this;
        }

        public VkMttScreenCreateInfo setExtent(VkExtent2D extent) {
            this.extent = extent;
            return this;
        }

        public VkMttScreenCreateInfo setShouldChangeExtentOnRecreate(boolean shouldChangeExtentOnRecreate) {
            this.shouldChangeExtentOnRecreate = shouldChangeExtentOnRecreate;
            return this;
        }

        public VkMttScreenCreateInfo setUseShadowMapping(boolean useShadowMapping) {
            this.useShadowMapping = useShadowMapping;
            return this;
        }

        public VkMttScreenCreateInfo setPpNaborNames(List<String> ppNaborNames) {
            this.ppNaborNames = ppNaborNames;
            return this;
        }

        public VkMttScreenCreateInfo setCustomizablePPNaborInfos(Map<String, CustomizablePostProcessingNaborInfo> customizablePPNaborInfos) {
            this.customizablePPNaborInfos = customizablePPNaborInfos;
            return this;
        }

        public VkMttScreenCreateInfo setFseNaborNames(List<String> fseNaborNames) {
            this.fseNaborNames = fseNaborNames;
            return this;
        }

        public VkMttScreenCreateInfo setFseNaborInfos(Map<String, FullScreenEffectNaborInfo> fseNaborInfos) {
            this.fseNaborInfos = fseNaborInfos;
            return this;
        }
    }

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

    private ShadowMappingNabor shadowMappingNabor;
    private int depthImageFormat;
    private int depthImageWidth;
    private int depthImageHeight;
    private int depthImageAspect;

    private PostProcessingNaborChain ppNaborChain;
    private FullScreenEffectNaborChain fseNaborChain;

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

    private void compileNabor(
            Nabor nabor,
            int colorImageFormat,
            int samplerFilter,
            int samplerMipmapMode,
            int samplerAddressMode,
            VkExtent2D extent) {
        nabor.compile(
                colorImageFormat,
                samplerFilter,
                samplerMipmapMode,
                samplerAddressMode,
                extent,
                commandPool,
                graphicsQueue,
                1
        );
    }

    public VkMttScreen(VkMttScreenCreateInfo createInfo) {
        this.device = createInfo.device;
        this.commandPool = createInfo.commandPool;
        this.graphicsQueue = createInfo.graphicsQueue;

        this.depthImageFormat = createInfo.depthImageFormat;
        this.depthImageWidth = createInfo.depthImageWidth;
        this.depthImageHeight = createInfo.depthImageHeight;
        this.depthImageAspect = createInfo.depthImageAspect;

        initialWidth = createInfo.extent.width();
        initialHeight = createInfo.extent.height();
        this.shouldChangeExtentOnRecreate = createInfo.shouldChangeExtentOnRecreate;

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);

        MttShaderSettings shaderSettings = MttShaderSettings
                .get()
                .orElse(MttShaderSettings.create());

        //GBuffer nabor
        URL gBufferAlbedoVertShaderResource = MttURLUtils.mustGetResourceURL(
                shaderSettings.gBuffer.albedo.vert.filepath,
                shaderSettings.gBuffer.albedo.vert.className
        );
        URL gBufferAlbedoFragShaderResource = MttURLUtils.mustGetResourceURL(
                shaderSettings.gBuffer.albedo.frag.filepath,
                shaderSettings.gBuffer.albedo.frag.className
        );
        URL gBufferPropertiesVertShaderResource = MttURLUtils.mustGetResourceURL(
                shaderSettings.gBuffer.properties.vert.filepath,
                shaderSettings.gBuffer.properties.vert.className
        );
        URL gBufferPropertiesFragShaderResource = MttURLUtils.mustGetResourceURL(
                shaderSettings.gBuffer.properties.frag.filepath,
                shaderSettings.gBuffer.properties.frag.className
        );

        gBufferNabor = new GBufferNabor(
                device,
                createInfo.albedoMSAASamples,
                depthImageFormat,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16_SFLOAT,
                gBufferAlbedoVertShaderResource,
                gBufferAlbedoFragShaderResource,
                gBufferPropertiesVertShaderResource,
                gBufferPropertiesFragShaderResource
        );
        this.compileNabor(
                gBufferNabor,
                createInfo.colorImageFormat,
                createInfo.samplerFilter,
                createInfo.samplerMipmapMode,
                createInfo.samplerAddressMode,
                createInfo.extent
        );

        //Primitive nabor
        URL primitiveVertShaderResource = MttURLUtils.mustGetResourceURL(
                shaderSettings.primitive.main.vert.filepath,
                shaderSettings.primitive.main.vert.className
        );
        URL primitiveFragShaderResource = MttURLUtils.mustGetResourceURL(
                shaderSettings.primitive.main.frag.filepath,
                shaderSettings.primitive.main.frag.className
        );

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
        this.compileNabor(
                primitiveNabor,
                createInfo.colorImageFormat,
                createInfo.samplerFilter,
                createInfo.samplerMipmapMode,
                createInfo.samplerAddressMode,
                createInfo.extent
        );

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
        this.compileNabor(
                primitiveFillNabor,
                createInfo.colorImageFormat,
                createInfo.samplerFilter,
                createInfo.samplerMipmapMode,
                createInfo.samplerAddressMode,
                createInfo.extent
        );

        //Merge-Scenes nabor
        URL mergeScenesVertShaderResource = MttURLUtils.mustGetResourceURL(
                shaderSettings.mergeScenes.main.vert.filepath,
                shaderSettings.mergeScenes.main.vert.className
        );
        URL mergeScenesFragShaderResource = MttURLUtils.mustGetResourceURL(
                shaderSettings.mergeScenes.main.frag.filepath,
                shaderSettings.mergeScenes.main.frag.className
        );

        mergeScenesNabor = new MergeScenesNabor(
                device,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                VK_FORMAT_R16G16B16A16_SFLOAT,
                mergeScenesVertShaderResource,
                mergeScenesFragShaderResource
        );
        this.compileNabor(
                mergeScenesNabor,
                createInfo.colorImageFormat,
                createInfo.samplerFilter,
                createInfo.samplerMipmapMode,
                createInfo.samplerAddressMode,
                createInfo.extent
        );

        //Shadow mapping
        if (createInfo.useShadowMapping) {
            URL shadowMappingPass1VertShaderResource = MttURLUtils.mustGetResourceURL(
                    shaderSettings.shadowMapping.pass1.vert.filepath,
                    shaderSettings.shadowMapping.pass1.vert.className
            );
            URL shadowMappingPass1FragShaderResource = MttURLUtils.mustGetResourceURL(
                    shaderSettings.shadowMapping.pass1.frag.filepath,
                    shaderSettings.shadowMapping.pass1.frag.className
            );
            URL shadowMappingPass2VertShaderResource = MttURLUtils.mustGetResourceURL(
                    shaderSettings.shadowMapping.pass2.vert.filepath,
                    shaderSettings.shadowMapping.pass2.vert.className
            );
            URL shadowMappingPass2FragShaderResource = MttURLUtils.mustGetResourceURL(
                    shaderSettings.shadowMapping.pass2.frag.filepath,
                    shaderSettings.shadowMapping.pass2.frag.className
            );

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
            this.compileNabor(
                    shadowMappingNabor,
                    createInfo.colorImageFormat,
                    createInfo.samplerFilter,
                    createInfo.samplerMipmapMode,
                    createInfo.samplerAddressMode,
                    createInfo.extent
            );
            this.createShadowMappingNaborUserDefImages(shadowMappingNabor);
        }

        //Post-processing nabors
        if (!createInfo.ppNaborNames.isEmpty()) {
            ppNaborChain = new PostProcessingNaborChain(
                    device,
                    commandPool,
                    graphicsQueue,
                    createInfo.colorImageFormat,
                    createInfo.samplerFilter,
                    createInfo.samplerMipmapMode,
                    createInfo.samplerAddressMode,
                    createInfo.extent,
                    createInfo.ppNaborNames,
                    createInfo.customizablePPNaborInfos
            );
        }

        //Full-screen effect nabors
        if (!createInfo.fseNaborNames.isEmpty()) {
            fseNaborChain = new FullScreenEffectNaborChain(
                    device,
                    commandPool,
                    graphicsQueue,
                    createInfo.colorImageFormat,
                    createInfo.samplerFilter,
                    createInfo.samplerMipmapMode,
                    VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER,
                    createInfo.extent,
                    createInfo.fseNaborNames,
                    createInfo.fseNaborInfos
            );
        }
    }

    @Deprecated
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
            List<String> ppNaborNames,
            Map<String, CustomizablePostProcessingNaborInfo> customizablePPNaborInfos,
            List<String> fseNaborNames,
            Map<String, FullScreenEffectNaborInfo> fseNaborInfos) {
        this(
                new VkMttScreenCreateInfo()
                        .setDevice(device)
                        .setCommandPool(commandPool)
                        .setGraphicsQueue(graphicsQueue)
                        .setDepthImageFormat(depthImageFormat)
                        .setDepthImageWidth(depthImageWidth)
                        .setDepthImageHeight(depthImageHeight)
                        .setDepthImageAspect(depthImageAspect)
                        .setColorImageFormat(colorImageFormat)
                        .setAlbedoMSAASamples(albedoMSAASamples)
                        .setSamplerFilter(samplerFilter)
                        .setSamplerMipmapMode(samplerMipmapMode)
                        .setSamplerAddressMode(samplerAddressMode)
                        .setExtent(extent)
                        .setShouldChangeExtentOnRecreate(shouldChangeExtentOnRecreate)
                        .setUseShadowMapping(useShadowMapping)
                        .setPpNaborNames(ppNaborNames)
                        .setCustomizablePPNaborInfos(customizablePPNaborInfos)
                        .setFseNaborNames(fseNaborNames)
                        .setFseNaborInfos(fseNaborInfos)
        );
    }

    public void recreate(int colorImageFormat, VkExtent2D extent) {
        VkExtent2D recreationExtent;
        if (shouldChangeExtentOnRecreate) {
            recreationExtent = extent;
        } else {
            recreationExtent = VkExtent2D.create().set(initialWidth, initialHeight);
        }

        gBufferNabor.recreate(colorImageFormat, recreationExtent);
        primitiveNabor.recreate(colorImageFormat, recreationExtent);
        primitiveFillNabor.recreate(colorImageFormat, recreationExtent);
        mergeScenesNabor.recreate(colorImageFormat, recreationExtent);

        if (shadowMappingNabor != null) {
            shadowMappingNabor.recreate(colorImageFormat, recreationExtent);
        }
        if (ppNaborChain != null) {
            ppNaborChain.recreate(colorImageFormat, recreationExtent);
        }
        if (fseNaborChain != null) {
            fseNaborChain.recreate(colorImageFormat, recreationExtent);
        }
    }

    public void cleanup() {
        quadDrawer.cleanup();

        gBufferNabor.cleanup(false);
        primitiveNabor.cleanup(false);
        primitiveFillNabor.cleanup(false);
        mergeScenesNabor.cleanup(false);

        if (shadowMappingNabor != null) {
            shadowMappingNabor.cleanup(false);
        }
        if (ppNaborChain != null) {
            ppNaborChain.cleanup();
        }
        if (fseNaborChain != null) {
            fseNaborChain.cleanup();
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
            ShadowMappingSettings shadowMappingSettings,
            PostProcessingProperties ppProperties,
            FullScreenEffectProperties fseProperties,
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
                    ppProperties.parallelLights,
                    ppProperties.spotlights,
                    components,
                    depthImageAspect,
                    shadowMappingSettings,
                    quadDrawer
            );
        }
        if (ppNaborChain != null) {
            long baseColorImageView;
            if (shadowMappingNabor != null) {
                baseColorImageView = shadowMappingNabor.getColorImageView();
            } else {
                baseColorImageView = mergeScenesNabor.getAlbedoImageView();
            }

            ppNaborChain.run(
                    camera,
                    ppProperties,
                    baseColorImageView,
                    mergeScenesNabor.getDepthImageView(),
                    mergeScenesNabor.getPositionImageView(),
                    mergeScenesNabor.getNormalImageView(),
                    mergeScenesNabor.getStencilImageView()
            );
        }
        if (fseNaborChain != null) {
            long baseColorImageView;
            if (ppNaborChain != null) {
                baseColorImageView = ppNaborChain.getLastPPNaborColorImageView();
            } else if (shadowMappingNabor != null) {
                baseColorImageView = shadowMappingNabor.getColorImageView();
            } else {
                baseColorImageView = mergeScenesNabor.getAlbedoImageView();
            }

            fseNaborChain.run(fseProperties, baseColorImageView);
        }

        components.forEach(VkMttComponent::cleanupLocally);
    }

    public long getColorImageView() {
        if (fseNaborChain != null) {
            return fseNaborChain.getLastFSENaborColorImageView();
        } else if (ppNaborChain != null) {
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
        //- Full-screen effect nabor
        //- Post-processing nabor
        //- Shadow mapping nabor
        //- Merge-scenes nabor
        if (imageType == ScreenImageType.COLOR) {
            //Acquire image from a full-screen effect nabor
            if (fseNaborChain != null) {
                return fseNaborChain.createBufferedImage(FullScreenEffectNabor.COLOR_ATTACHMENT_INDEX, pixelFormat);
            }
            //Acquire image from a post-processing nabor
            else if (ppNaborChain != null) {
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
