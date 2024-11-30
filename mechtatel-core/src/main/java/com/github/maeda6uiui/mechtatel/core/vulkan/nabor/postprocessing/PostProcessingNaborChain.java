package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.MttShaderSettings;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.postprocessing.CustomizablePostProcessingNaborInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.LightingInfo;
import com.github.maeda6uiui.mechtatel.core.util.MttURLUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Chain of post-processing nabors
 *
 * @author maeda6uiui
 */
public class PostProcessingNaborChain {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private Map<String, PostProcessingNabor> ppNabors;
    private Map<String, CustomizablePostProcessingNaborInfo> customizablePPNaborInfos;
    private PostProcessingNabor lastPPNabor;

    private QuadDrawer quadDrawer;

    public PostProcessingNaborChain(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            int colorImageFormat,
            int samplerFilter,
            int samplerMipmapMode,
            int samplerAddressMode,
            VkExtent2D extent,
            List<String> naborNames,
            Map<String, CustomizablePostProcessingNaborInfo> customizablePPNaborInfos,
            Map<String, List<Long>> vertShaderModulesStorage,
            Map<String, List<Long>> fragShaderModulesStorage) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        ppNabors = new LinkedHashMap<>();
        this.customizablePPNaborInfos = customizablePPNaborInfos;

        //Get shader URLs =====
        MttShaderSettings shaderSettings = MttShaderSettings
                .get()
                .orElse(MttShaderSettings.create());

        URL fogVertShaderResource;
        URL fogFragShaderResource;
        URL parallelLightVertShaderResource;
        URL parallelLightFragShaderResource;
        URL pointLightVertShaderResource;
        URL pointLightFragShaderResource;
        URL spotlightVertShaderResource;
        URL spotlightFragShaderResource;
        URL simpleBlurVertShaderResource;
        URL simpleBlurFragShaderResource;
        try {
            fogVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.fog.vert.filepath,
                    shaderSettings.postProcessing.fog.vert.external
            );
            fogFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.fog.frag.filepath,
                    shaderSettings.postProcessing.fog.frag.external
            );

            parallelLightVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.parallelLight.vert.filepath,
                    shaderSettings.postProcessing.parallelLight.vert.external
            );
            parallelLightFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.parallelLight.frag.filepath,
                    shaderSettings.postProcessing.parallelLight.frag.external
            );

            pointLightVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.pointLight.vert.filepath,
                    shaderSettings.postProcessing.pointLight.vert.external
            );
            pointLightFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.pointLight.frag.filepath,
                    shaderSettings.postProcessing.pointLight.frag.external
            );

            spotlightVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.spotlight.vert.filepath,
                    shaderSettings.postProcessing.spotlight.vert.external
            );
            spotlightFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.spotlight.frag.filepath,
                    shaderSettings.postProcessing.spotlight.frag.external
            );

            simpleBlurVertShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.simpleBlur.vert.filepath,
                    shaderSettings.postProcessing.simpleBlur.vert.external
            );
            simpleBlurFragShaderResource = MttURLUtils.getResourceURL(
                    shaderSettings.postProcessing.simpleBlur.frag.filepath,
                    shaderSettings.postProcessing.simpleBlur.frag.external
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        //==========

        for (var naborName : naborNames) {
            PostProcessingNabor ppNabor;

            CustomizablePostProcessingNaborInfo customizablePPNaborInfo = customizablePPNaborInfos
                    .entrySet()
                    .stream()
                    .filter(v -> v.getKey().equals(naborName))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (customizablePPNaborInfo != null) {
                ppNabor = new CustomizablePostProcessingNabor(
                        device,
                        customizablePPNaborInfo.getVertShaderResource(),
                        customizablePPNaborInfo.getFragShaderResource(),
                        customizablePPNaborInfo.getUniformResourceTypes()
                );
            } else {
                ppNabor = switch (naborName) {
                    case "pp.fog" -> new FogNabor(device, fogVertShaderResource, fogFragShaderResource);
                    case "pp.parallel_light" ->
                            new ParallelLightNabor(device, parallelLightVertShaderResource, parallelLightFragShaderResource);
                    case "pp.point_light" ->
                            new PointLightNabor(device, pointLightVertShaderResource, pointLightFragShaderResource);
                    case "pp.spotlight" ->
                            new SpotlightNabor(device, spotlightVertShaderResource, spotlightFragShaderResource);
                    case "pp.simple_blur" ->
                            new SimpleBlurNabor(device, simpleBlurVertShaderResource, simpleBlurFragShaderResource);
                    default -> throw new IllegalArgumentException("Unknown nabor name specified: " + naborName);
                };
            }

            if (vertShaderModulesStorage.containsKey(naborName)) {
                var vertShaderModules = vertShaderModulesStorage.get(naborName);
                var fragShaderModules = fragShaderModulesStorage.get(naborName);

                ppNabor.compile(
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
                ppNabor.compile(
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

            ppNabors.put(naborName, ppNabor);
            lastPPNabor = ppNabor;
        }

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);
    }

    public void recreate(int imageFormat, VkExtent2D extent) {
        for (var ppNabor : ppNabors.values()) {
            ppNabor.recreate(imageFormat, extent);
        }
    }

    public void cleanup() {
        ppNabors.forEach((k, nabor) -> nabor.cleanup(false));
        quadDrawer.cleanup();
    }

    private void updateStandardPPNaborUBOs(
            String naborName,
            PostProcessingNabor ppNabor,
            Camera camera,
            PostProcessingProperties ppProperties) {
        switch (naborName) {
            case "pp.fog": {
                long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                var cameraUBO = new CameraUBO(camera);
                cameraUBO.update(device, cameraUBOMemory);

                long fogUBOMemory = ppNabor.getUniformBufferMemory(1);
                var fogUBO = new FogUBO(ppProperties.fog);
                fogUBO.update(device, fogUBOMemory);
            }
            break;

            case "pp.parallel_light": {
                long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                var cameraUBO = new CameraUBO(camera);
                cameraUBO.update(device, cameraUBOMemory);

                var lightingInfo = new LightingInfo();
                lightingInfo.setNumLights(ppProperties.parallelLights.size());
                lightingInfo.setAmbientColor(ppProperties.parallelLightAmbientColor);

                long lightingInfoUBOMemory = ppNabor.getUniformBufferMemory(1);
                var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                lightingInfoUBO.update(device, lightingInfoUBOMemory);

                long lightUBOMemory = ppNabor.getUniformBufferMemory(2);
                for (int i = 0; i < ppProperties.parallelLights.size(); i++) {
                    var lightUBO = new ParallelLightUBO(ppProperties.parallelLights.get(i));
                    lightUBO.update(device, lightUBOMemory, i);
                }
            }
            break;

            case "pp.point_light": {
                long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                var cameraUBO = new CameraUBO(camera);
                cameraUBO.update(device, cameraUBOMemory);

                var lightingInfo = new LightingInfo();
                lightingInfo.setNumLights(ppProperties.pointLights.size());
                lightingInfo.setAmbientColor(ppProperties.pointLightAmbientColor);

                long lightingInfoUBOMemory = ppNabor.getUniformBufferMemory(1);
                var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                lightingInfoUBO.update(device, lightingInfoUBOMemory);

                long lightUBOMemory = ppNabor.getUniformBufferMemory(2);
                for (int i = 0; i < ppProperties.pointLights.size(); i++) {
                    var lightUBO = new PointLightUBO(ppProperties.pointLights.get(i));
                    lightUBO.update(device, lightUBOMemory, i);
                }
            }
            break;

            case "pp.spotlight": {
                long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                var cameraUBO = new CameraUBO(camera);
                cameraUBO.update(device, cameraUBOMemory);

                var lightingInfo = new LightingInfo();
                lightingInfo.setNumLights(ppProperties.spotlights.size());
                lightingInfo.setAmbientColor(ppProperties.spotlightAmbientColor);

                long lightingInfoUBOMemory = ppNabor.getUniformBufferMemory(1);
                var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                lightingInfoUBO.update(device, lightingInfoUBOMemory);

                long lightUBOMemory = ppNabor.getUniformBufferMemory(2);
                for (int i = 0; i < ppProperties.spotlights.size(); i++) {
                    var lightUBO = new SpotlightUBO(ppProperties.spotlights.get(i));
                    lightUBO.update(device, lightUBOMemory, i);
                }
            }
            break;

            case "pp.simple_blur": {
                long blurInfoUBOMemory = ppNabor.getUniformBufferMemory(0);
                var blurInfoUBO = new SimpleBlurInfoUBO(ppProperties.simpleBlurInfo);
                blurInfoUBO.update(device, blurInfoUBOMemory);
            }
            break;

            default:
                throw new IllegalArgumentException("Unsupported nabor specified: " + naborName);
        }
    }

    private void updateCustomizablePPNaborUBOs(
            PostProcessingNabor ppNabor,
            CustomizablePostProcessingNaborInfo customizablePPNaborInfo,
            Camera camera,
            PostProcessingProperties ppProperties) {
        List<CustomizablePostProcessingNaborInfo.UniformResourceType> uniformResourceTypes = customizablePPNaborInfo.getUniformResourceTypes();
        for (int i = 0; i < uniformResourceTypes.size(); i++) {
            long uboMemory = ppNabor.getUniformBufferMemory(i);

            var uniformResourceType = uniformResourceTypes.get(i);
            switch (uniformResourceType) {
                case CAMERA -> {
                    var cameraUBO = new CameraUBO(camera);
                    cameraUBO.update(device, uboMemory);
                }
                case FOG -> {
                    var fogUBO = new FogUBO(ppProperties.fog);
                    fogUBO.update(device, uboMemory);
                }
                case LIGHTING_INFO -> {
                    var lightingInfo = new LightingInfo();
                    switch (customizablePPNaborInfo.getLightingType()) {
                        case PARALLEL -> {
                            lightingInfo.setNumLights(ppProperties.parallelLights.size());
                            lightingInfo.setAmbientColor(ppProperties.parallelLightAmbientColor);
                        }
                        case POINT -> {
                            lightingInfo.setNumLights(ppProperties.pointLights.size());
                            lightingInfo.setAmbientColor(ppProperties.pointLightAmbientColor);
                        }
                        case SPOT -> {
                            lightingInfo.setNumLights(ppProperties.spotlights.size());
                            lightingInfo.setAmbientColor(ppProperties.spotlightAmbientColor);
                        }
                    }

                    lightingInfo.setLightingClampMin(customizablePPNaborInfo.getLightingClampMin());
                    lightingInfo.setLightingClampMax(customizablePPNaborInfo.getLightingClampMax());

                    var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                    lightingInfoUBO.update(device, uboMemory);
                }
                case PARALLEL_LIGHT -> {
                    for (int j = 0; j < ppProperties.parallelLights.size(); j++) {
                        var lightUBO = new ParallelLightUBO(ppProperties.parallelLights.get(j));
                        lightUBO.update(device, uboMemory, j);
                    }
                }
                case POINT_LIGHT -> {
                    for (int j = 0; j < ppProperties.pointLights.size(); j++) {
                        var lightUBO = new PointLightUBO(ppProperties.pointLights.get(j));
                        lightUBO.update(device, uboMemory, j);
                    }
                }
                case SIMPLE_BLUR -> {
                    var blurInfoUBO = new SimpleBlurInfoUBO(ppProperties.simpleBlurInfo);
                    blurInfoUBO.update(device, uboMemory);
                }
                case SPOTLIGHT -> {
                    for (int j = 0; j < ppProperties.spotlights.size(); j++) {
                        var lightUBO = new SpotlightUBO(ppProperties.spotlights.get(j));
                        lightUBO.update(device, uboMemory, j);
                    }
                }
            }
        }
    }

    public void run(
            Camera camera,
            PostProcessingProperties ppProperties,
            long baseColorImageView,
            long baseDepthImageView,
            long basePositionImageView,
            long baseNormalImageView,
            long baseStencilImageView) {
        PostProcessingNabor previousPPNabor = null;
        for (var entry : ppNabors.entrySet()) {
            String naborName = entry.getKey();
            PostProcessingNabor ppNabor = entry.getValue();

            if (customizablePPNaborInfos.containsKey(naborName)) {
                CustomizablePostProcessingNaborInfo customizablePPNaborInfo = customizablePPNaborInfos.get(naborName);
                this.updateCustomizablePPNaborUBOs(
                        ppNabor,
                        customizablePPNaborInfo,
                        camera,
                        ppProperties
                );
            } else {
                this.updateStandardPPNaborUBOs(
                        naborName,
                        ppNabor,
                        camera,
                        ppProperties
                );
            }

            try (MemoryStack stack = MemoryStack.stackPush()) {
                VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.calloc(stack);
                renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
                renderPassInfo.renderPass(ppNabor.getRenderPass());
                renderPassInfo.framebuffer(ppNabor.getFramebuffer(0));

                VkRect2D renderArea = VkRect2D.calloc(stack);
                renderArea.offset(VkOffset2D.calloc(stack).set(0, 0));
                renderArea.extent(ppNabor.getExtent());
                renderPassInfo.renderArea(renderArea);

                VkClearValue.Buffer clearValues = VkClearValue.calloc(1, stack);
                clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
                renderPassInfo.pClearValues(clearValues);

                VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, ppNabor.getGraphicsPipeline(0));

                    //First post-processing
                    if (previousPPNabor == null) {
                        ppNabor.bindImages(
                                commandBuffer,
                                1,
                                0,
                                Arrays.asList(
                                        baseColorImageView,
                                        baseDepthImageView,
                                        basePositionImageView,
                                        baseNormalImageView,
                                        baseStencilImageView
                                )
                        );
                    } else {
                        ppNabor.bindImages(
                                commandBuffer,
                                1,
                                0,
                                Arrays.asList(
                                        previousPPNabor.getColorImageView(),
                                        baseDepthImageView,
                                        basePositionImageView,
                                        baseNormalImageView,
                                        baseStencilImageView
                                )
                        );
                    }

                    quadDrawer.draw(commandBuffer);
                }
                vkCmdEndRenderPass(commandBuffer);

                CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
            }

            ppNabor.transitionColorImageLayout(commandPool, graphicsQueue);
            previousPPNabor = ppNabor;
        }
    }

    public Map<String, List<Long>> getVertShaderModules() {
        var vertShaderModules = new HashMap<String, List<Long>>();

        for (var entry : ppNabors.entrySet()) {
            String naborName = entry.getKey();
            PostProcessingNabor ppNabor = entry.getValue();

            vertShaderModules.put(naborName, ppNabor.getVertShaderModules());
        }

        return vertShaderModules;
    }

    public Map<String, List<Long>> getFragShaderModules() {
        var fragShaderModules = new HashMap<String, List<Long>>();

        for (var entry : ppNabors.entrySet()) {
            String naborName = entry.getKey();
            PostProcessingNabor ppNabor = entry.getValue();

            fragShaderModules.put(naborName, ppNabor.getFragShaderModules());
        }

        return fragShaderModules;
    }

    public long getLastPPNaborColorImageView() {
        return lastPPNabor.getColorImageView();
    }

    public BufferedImage createBufferedImage(int imageIndex, PixelFormat pixelFormat) {
        return lastPPNabor.createBufferedImage(commandPool, graphicsQueue, imageIndex, pixelFormat);
    }
}
