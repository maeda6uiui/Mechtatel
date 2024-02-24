package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.nabor.CustomizableNaborInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.LightingInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.MergeScenesNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.shadow.ShadowMappingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.awt.image.BufferedImage;
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
    private Map<String, CustomizableNaborInfo> customizableNaborInfos;
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
            Map<String, CustomizableNaborInfo> customizableNaborInfos,
            Map<String, List<Long>> vertShaderModulesStorage,
            Map<String, List<Long>> fragShaderModulesStorage) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        ppNabors = new LinkedHashMap<>();
        this.customizableNaborInfos = customizableNaborInfos;

        for (var naborName : naborNames) {
            PostProcessingNabor ppNabor;

            CustomizableNaborInfo customizableNaborInfo = customizableNaborInfos
                    .entrySet()
                    .stream()
                    .filter(v -> v.getKey().equals(naborName))
                    .map(Map.Entry::getValue)
                    .findFirst()
                    .orElse(null);
            if (customizableNaborInfo != null) {
                ppNabor = new CustomizableNabor(
                        device,
                        customizableNaborInfo.getVertShaderResource(),
                        customizableNaborInfo.getFragShaderResource(),
                        customizableNaborInfo.getUniformResourceTypes()
                );
            } else {
                ppNabor = switch (naborName) {
                    case "fog" -> new FogNabor(device);
                    case "parallel_light" -> new ParallelLightNabor(device);
                    case "point_light" -> new PointLightNabor(device);
                    case "spotlight" -> new SpotlightNabor(device);
                    case "simple_blur" -> new SimpleBlurNabor(device);
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

    private void updateStandardNaborUBOs(
            String naborName,
            PostProcessingNabor ppNabor,
            Camera camera,
            Fog fog,
            List<ParallelLight> parallelLights,
            Vector3f parallelLightAmbientColor,
            List<PointLight> pointLights,
            Vector3f pointLightAmbientColor,
            List<Spotlight> spotlights,
            Vector3f spotlightAmbientColor,
            SimpleBlurInfo simpleBlurInfo) {
        switch (naborName) {
            case "fog": {
                long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                var cameraUBO = new CameraUBO(camera);
                cameraUBO.update(device, cameraUBOMemory);

                long fogUBOMemory = ppNabor.getUniformBufferMemory(1);
                var fogUBO = new FogUBO(fog);
                fogUBO.update(device, fogUBOMemory);
            }
            break;

            case "parallel_light": {
                long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                var cameraUBO = new CameraUBO(camera);
                cameraUBO.update(device, cameraUBOMemory);

                var lightingInfo = new LightingInfo();
                lightingInfo.setNumLights(parallelLights.size());
                lightingInfo.setAmbientColor(parallelLightAmbientColor);

                long lightingInfoUBOMemory = ppNabor.getUniformBufferMemory(1);
                var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                lightingInfoUBO.update(device, lightingInfoUBOMemory);

                long lightUBOMemory = ppNabor.getUniformBufferMemory(2);
                for (int i = 0; i < parallelLights.size(); i++) {
                    var lightUBO = new ParallelLightUBO(parallelLights.get(i));
                    lightUBO.update(device, lightUBOMemory, i);
                }
            }
            break;

            case "point_light": {
                long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                var cameraUBO = new CameraUBO(camera);
                cameraUBO.update(device, cameraUBOMemory);

                var lightingInfo = new LightingInfo();
                lightingInfo.setNumLights(pointLights.size());
                lightingInfo.setAmbientColor(pointLightAmbientColor);

                long lightingInfoUBOMemory = ppNabor.getUniformBufferMemory(1);
                var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                lightingInfoUBO.update(device, lightingInfoUBOMemory);

                long lightUBOMemory = ppNabor.getUniformBufferMemory(2);
                for (int i = 0; i < pointLights.size(); i++) {
                    var lightUBO = new PointLightUBO(pointLights.get(i));
                    lightUBO.update(device, lightUBOMemory, i);
                }
            }
            break;

            case "spotlight": {
                long cameraUBOMemory = ppNabor.getUniformBufferMemory(0);
                var cameraUBO = new CameraUBO(camera);
                cameraUBO.update(device, cameraUBOMemory);

                var lightingInfo = new LightingInfo();
                lightingInfo.setNumLights(spotlights.size());
                lightingInfo.setAmbientColor(spotlightAmbientColor);

                long lightingInfoUBOMemory = ppNabor.getUniformBufferMemory(1);
                var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                lightingInfoUBO.update(device, lightingInfoUBOMemory);

                long lightUBOMemory = ppNabor.getUniformBufferMemory(2);
                for (int i = 0; i < spotlights.size(); i++) {
                    var lightUBO = new SpotlightUBO(spotlights.get(i));
                    lightUBO.update(device, lightUBOMemory, i);
                }
            }
            break;

            case "simple_blur": {
                long blurInfoUBOMemory = ppNabor.getUniformBufferMemory(0);
                var blurInfoUBO = new SimpleBlurInfoUBO(simpleBlurInfo);
                blurInfoUBO.update(device, blurInfoUBOMemory);
            }
            break;

            default:
                throw new IllegalArgumentException("Unsupported nabor specified: " + naborName);
        }
    }

    private void updateCustomizableNaborUBOs(
            PostProcessingNabor ppNabor,
            CustomizableNaborInfo customizableNaborInfo,
            Camera camera,
            Fog fog,
            List<ParallelLight> parallelLights,
            Vector3f parallelLightAmbientColor,
            List<PointLight> pointLights,
            Vector3f pointLightAmbientColor,
            List<Spotlight> spotlights,
            Vector3f spotlightAmbientColor,
            SimpleBlurInfo simpleBlurInfo) {
        List<CustomizableNaborInfo.UniformResourceType> uniformResourceTypes = customizableNaborInfo.getUniformResourceTypes();
        for (int i = 0; i < uniformResourceTypes.size(); i++) {
            long uboMemory = ppNabor.getUniformBufferMemory(i);

            var uniformResourceType = uniformResourceTypes.get(i);
            switch (uniformResourceType) {
                case CAMERA -> {
                    var cameraUBO = new CameraUBO(camera);
                    cameraUBO.update(device, uboMemory);
                }
                case FOG -> {
                    var fogUBO = new FogUBO(fog);
                    fogUBO.update(device, uboMemory);
                }
                case LIGHTING_INFO -> {
                    var lightingInfo = new LightingInfo();
                    switch (customizableNaborInfo.getLightingType()) {
                        case PARALLEL -> {
                            lightingInfo.setNumLights(parallelLights.size());
                            lightingInfo.setAmbientColor(parallelLightAmbientColor);
                        }
                        case POINT -> {
                            lightingInfo.setNumLights(pointLights.size());
                            lightingInfo.setAmbientColor(pointLightAmbientColor);
                        }
                        case SPOT -> {
                            lightingInfo.setNumLights(spotlights.size());
                            lightingInfo.setAmbientColor(spotlightAmbientColor);
                        }
                    }

                    lightingInfo.setLightingClampMin(customizableNaborInfo.getLightingClampMin());
                    lightingInfo.setLightingClampMax(customizableNaborInfo.getLightingClampMax());

                    var lightingInfoUBO = new LightingInfoUBO(lightingInfo);
                    lightingInfoUBO.update(device, uboMemory);
                }
                case PARALLEL_LIGHT -> {
                    for (int j = 0; j < parallelLights.size(); j++) {
                        var lightUBO = new ParallelLightUBO(parallelLights.get(j));
                        lightUBO.update(device, uboMemory, j);
                    }
                }
                case POINT_LIGHT -> {
                    for (int j = 0; j < pointLights.size(); j++) {
                        var lightUBO = new PointLightUBO(pointLights.get(j));
                        lightUBO.update(device, uboMemory, j);
                    }
                }
                case SIMPLE_BLUR -> {
                    var blurInfoUBO = new SimpleBlurInfoUBO(simpleBlurInfo);
                    blurInfoUBO.update(device, uboMemory);
                }
                case SPOTLIGHT -> {
                    for (int j = 0; j < spotlights.size(); j++) {
                        var lightUBO = new SpotlightUBO(spotlights.get(j));
                        lightUBO.update(device, uboMemory, j);
                    }
                }
            }
        }
    }

    public void run(
            Camera camera,
            Fog fog,
            List<ParallelLight> parallelLights,
            Vector3f parallelLightAmbientColor,
            List<PointLight> pointLights,
            Vector3f pointLightAmbientColor,
            List<Spotlight> spotlights,
            Vector3f spotlightAmbientColor,
            SimpleBlurInfo simpleBlurInfo,
            MergeScenesNabor lastMergeNabor,
            ShadowMappingNabor shadowMappingNabor) {
        PostProcessingNabor previousPPNabor = null;
        for (var entry : ppNabors.entrySet()) {
            String naborName = entry.getKey();
            PostProcessingNabor ppNabor = entry.getValue();

            if (customizableNaborInfos.containsKey(naborName)) {
                CustomizableNaborInfo customizableNaborInfo = customizableNaborInfos.get(naborName);
                this.updateCustomizableNaborUBOs(
                        ppNabor,
                        customizableNaborInfo,
                        camera,
                        fog,
                        parallelLights,
                        parallelLightAmbientColor,
                        pointLights,
                        pointLightAmbientColor,
                        spotlights,
                        spotlightAmbientColor,
                        simpleBlurInfo
                );
            } else {
                this.updateStandardNaborUBOs(
                        naborName,
                        ppNabor,
                        camera,
                        fog,
                        parallelLights,
                        parallelLightAmbientColor,
                        pointLights,
                        pointLightAmbientColor,
                        spotlights,
                        spotlightAmbientColor,
                        simpleBlurInfo
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
                        if (shadowMappingNabor != null) {
                            ppNabor.bindImages(
                                    commandBuffer,
                                    0,
                                    1,
                                    Arrays.asList(
                                            shadowMappingNabor.getColorImageView(),
                                            lastMergeNabor.getDepthImageView(),
                                            lastMergeNabor.getPositionImageView(),
                                            lastMergeNabor.getNormalImageView()
                                    )
                            );
                        } else {
                            ppNabor.bindImages(
                                    commandBuffer,
                                    0,
                                    1,
                                    Arrays.asList(
                                            lastMergeNabor.getAlbedoImageView(),
                                            lastMergeNabor.getDepthImageView(),
                                            lastMergeNabor.getPositionImageView(),
                                            lastMergeNabor.getNormalImageView()
                                    )
                            );
                        }
                    } else {
                        ppNabor.bindImages(
                                commandBuffer,
                                0,
                                1,
                                Arrays.asList(
                                        previousPPNabor.getColorImageView(),
                                        lastMergeNabor.getDepthImageView(),
                                        lastMergeNabor.getPositionImageView(),
                                        lastMergeNabor.getNormalImageView()
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
