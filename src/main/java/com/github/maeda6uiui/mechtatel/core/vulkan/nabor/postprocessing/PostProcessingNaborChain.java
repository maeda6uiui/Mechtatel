package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.LightingInfo;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.drawer.QuadDrawer;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.MergeScenesNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.shadow.ShadowMappingNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.shadow.ShadowMappingNaborRunner;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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

    private int depthImageFormat;
    private int depthImageWidth;
    private int depthImageHeight;
    private int depthImageAspect;

    private Map<String, PostProcessingNabor> ppNabors;
    private PostProcessingNabor lastPPNabor;

    private QuadDrawer quadDrawer;

    private void createShadowMappingNaborUserDefImages(PostProcessingNabor shadowMappingNabor) {
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

    public PostProcessingNaborChain(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            int depthImageFormat,
            int depthImageWidth,
            int depthImageHeight,
            int depthImageAspect,
            int colorImageFormat,
            VkExtent2D extent,
            List<String> naborNames,
            Map<String, List<Long>> vertShaderModulesStorage,
            Map<String, List<Long>> fragShaderModulesStorage) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        this.depthImageFormat = depthImageFormat;
        this.depthImageWidth = depthImageWidth;
        this.depthImageHeight = depthImageHeight;
        this.depthImageAspect = depthImageAspect;

        ppNabors = new LinkedHashMap<>();

        for (var naborName : naborNames) {
            PostProcessingNabor ppNabor;

            switch (naborName) {
                case "fog":
                    ppNabor = new FogNabor(device);
                    break;
                case "parallel_light":
                    ppNabor = new ParallelLightNabor(device);
                    break;
                case "point_light":
                    ppNabor = new PointLightNabor(device);
                    break;
                case "shadow_mapping":
                    ppNabor = new ShadowMappingNabor(device, depthImageFormat, depthImageWidth, depthImageHeight);
                    this.createShadowMappingNaborUserDefImages(ppNabor);
                    break;
                case "spotlight":
                    ppNabor = new SpotlightNabor(device);
                    break;
                default:
                    String msg = String.format("Unsupported nabor specified: %s", naborName);
                    throw new IllegalArgumentException(msg);
            }

            if (vertShaderModulesStorage.containsKey(naborName)) {
                var vertShaderModules = vertShaderModulesStorage.get(naborName);
                var fragShaderModules = fragShaderModulesStorage.get(naborName);

                ppNabor.compile(
                        colorImageFormat,
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
                        extent,
                        commandPool,
                        graphicsQueue,
                        1);
            }

            ppNabors.put(naborName, ppNabor);
            lastPPNabor = ppNabor;
        }

        quadDrawer = new QuadDrawer(device, commandPool, graphicsQueue);
    }

    public void recreate(int imageFormat, VkExtent2D extent) {
        for (var entry : ppNabors.entrySet()) {
            String naborName = entry.getKey();
            PostProcessingNabor ppNabor = entry.getValue();

            ppNabor.recreate(imageFormat, extent);

            if (naborName.equals("shadow_mapping")) {
                this.createShadowMappingNaborUserDefImages(ppNabor);
            }
        }
    }

    public void cleanup() {
        ppNabors.forEach((k, nabor) -> nabor.cleanup(false));
        quadDrawer.cleanup();
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
            ShadowMappingSettings shadowMappingSettings,
            MergeScenesNabor lastMergeNabor,
            List<VkComponent> components) {
        PostProcessingNabor previousPPNabor = null;
        for (var entry : ppNabors.entrySet()) {
            String naborName = entry.getKey();
            PostProcessingNabor ppNabor = entry.getValue();

            if (naborName.equals("shadow_mapping")) {
                ShadowMappingNaborRunner.runShadowMappingNabor(
                        device,
                        commandPool,
                        graphicsQueue,
                        lastMergeNabor,
                        previousPPNabor,
                        ppNabor,
                        parallelLights,
                        spotlights,
                        components,
                        depthImageAspect,
                        shadowMappingSettings,
                        quadDrawer);

                previousPPNabor = ppNabor;

                continue;
            }

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

                default:
                    throw new RuntimeException("Unsupported nabor specified");
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
                                0,
                                lastMergeNabor.getAlbedoImageView(),
                                lastMergeNabor.getDepthImageView(),
                                lastMergeNabor.getPositionImageView(),
                                lastMergeNabor.getNormalImageView());
                    } else {
                        previousPPNabor.transitionColorImageLayout(commandPool, graphicsQueue);

                        ppNabor.bindImages(
                                commandBuffer,
                                0,
                                previousPPNabor.getColorImageView(),
                                lastMergeNabor.getDepthImageView(),
                                lastMergeNabor.getPositionImageView(),
                                lastMergeNabor.getNormalImageView());
                    }

                    quadDrawer.draw(commandBuffer);
                }
                vkCmdEndRenderPass(commandBuffer);

                CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
            }

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

    public void transitionLastPPNaborColorImage() {
        lastPPNabor.transitionColorImageLayout(commandPool, graphicsQueue);
    }

    public long getLastPPNaborColorImageView() {
        return lastPPNabor.getColorImageView();
    }

    public void save(String srcImageFormat, String outputFilepath) throws IOException {
        lastPPNabor.save(commandPool, graphicsQueue, 0, srcImageFormat, outputFilepath);
    }
}
