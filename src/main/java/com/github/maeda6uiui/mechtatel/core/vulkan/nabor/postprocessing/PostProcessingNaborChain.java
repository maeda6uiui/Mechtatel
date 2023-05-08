package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.shadow.ShadowMappingNabor;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkQueue;

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
    private Map<String, PostProcessingNabor> nabors;
    private PostProcessingNabor lastNabor;

    private void createShadowMappingNaborUserDefImages(
            PostProcessingNabor shadowMappingNabor,
            int depthImageFormat,
            int depthImageWidth,
            int depthImageHeight) {
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
            int depthImageFormat,
            int depthImageWidth,
            int depthImageHeight,
            int swapchainImageFormat,
            VkExtent2D swapchainExtent,
            long commandPool,
            VkQueue graphicsQueue,
            List<String> naborNames) {
        nabors = new LinkedHashMap<>();

        for (var naborName : naborNames) {
            PostProcessingNabor nabor;

            switch (naborName) {
                case "fog":
                    nabor = new FogNabor(device);
                    break;
                case "parallel_light":
                    nabor = new ParallelLightNabor(device);
                    break;
                case "point_light":
                    nabor = new PointLightNabor(device);
                    break;
                case "shadow_mapping":
                    nabor = new ShadowMappingNabor(device, depthImageFormat, depthImageWidth, depthImageHeight);
                    this.createShadowMappingNaborUserDefImages(nabor, depthImageFormat, depthImageWidth, depthImageHeight);
                    break;
                case "spotlight":
                    nabor = new SpotlightNabor(device);
                    break;
                default:
                    String msg = String.format("Unsupported nabor specified: %s", naborName);
                    throw new IllegalArgumentException(msg);
            }

            nabor.compile(
                    swapchainImageFormat,
                    swapchainExtent,
                    commandPool,
                    graphicsQueue,
                    1);
            nabors.put(naborName, nabor);
            lastNabor = nabor;
        }
    }

    public void recreate(
            int swapchainImageFormat,
            VkExtent2D swapchainExtent,
            int depthImageFormat,
            int depthImageWidth,
            int depthImageHeight) {
        for (var entry : nabors.entrySet()) {
            String naborName = entry.getKey();
            PostProcessingNabor nabor = entry.getValue();

            nabor.recreate(swapchainImageFormat, swapchainExtent);

            if (naborName.equals("shadow_mapping")) {
                this.createShadowMappingNaborUserDefImages(nabor, depthImageFormat, depthImageWidth, depthImageHeight);
            }
        }
    }

    public void cleanup() {
        nabors.forEach((k, nabor) -> nabor.cleanup(false));
    }

    public long getLastNaborAlbedoImage() {
        return lastNabor.getImage(0);
    }
}
