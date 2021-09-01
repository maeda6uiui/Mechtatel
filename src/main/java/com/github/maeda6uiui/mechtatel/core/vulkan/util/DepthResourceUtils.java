package com.github.maeda6uiui.mechtatel.core.vulkan.util;

import com.github.maeda6uiui.mechtatel.core.vulkan.creator.ImageViewCreator;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFormatProperties;
import org.lwjgl.vulkan.VkQueue;

import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Utility methods for depth resources
 *
 * @author maeda
 */
public class DepthResourceUtils {
    public static class DepthResources {
        public long depthImage;
        public long depthImageMemory;
        public long depthImageView;
    }

    public static int findSupportedFormat(VkDevice device, IntBuffer formatCandidates, int tiling, int features) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFormatProperties props = VkFormatProperties.callocStack(stack);

            for (int i = 0; i < formatCandidates.capacity(); i++) {
                int format = formatCandidates.get(i);
                vkGetPhysicalDeviceFormatProperties(device.getPhysicalDevice(), format, props);

                if (tiling == VK_IMAGE_TILING_LINEAR && (props.linearTilingFeatures() & features) == features) {
                    return format;
                } else if (tiling == VK_IMAGE_TILING_OPTIMAL && (props.optimalTilingFeatures() & features) == features) {
                    return format;
                }
            }
        }

        throw new RuntimeException("Failed to find a supported format");
    }

    public static int findDepthFormat(VkDevice device) {
        return findSupportedFormat(
                device,
                MemoryStack.stackGet().ints(VK_FORMAT_D32_SFLOAT, VK_FORMAT_D32_SFLOAT_S8_UINT, VK_FORMAT_D24_UNORM_S8_UINT),
                VK_IMAGE_TILING_OPTIMAL,
                VK_FORMAT_FEATURE_DEPTH_STENCIL_ATTACHMENT_BIT);
    }

    public static boolean hasStencilComponent(int format) {
        return format == VK_FORMAT_D32_SFLOAT_S8_UINT || format == VK_FORMAT_D24_UNORM_S8_UINT;
    }

    public static DepthResources createDepthResources(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkExtent2D swapchainExtent,
            int msaaSamples) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int depthFormat = findDepthFormat(device);

            LongBuffer pDepthImage = stack.mallocLong(1);
            LongBuffer pDepthImageMemory = stack.mallocLong(1);

            ImageUtils.createImage(
                    device,
                    swapchainExtent.width(),
                    swapchainExtent.height(),
                    1,
                    msaaSamples,
                    depthFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pDepthImage,
                    pDepthImageMemory);
            long depthImage = pDepthImage.get(0);
            long depthImageMemory = pDepthImageMemory.get(0);

            long depthImageView = ImageViewCreator.createImageView(
                    device, depthImage, depthFormat, VK_IMAGE_ASPECT_DEPTH_BIT, 1);

            ImageUtils.transitionImageLayout(
                    device,
                    commandPool,
                    graphicsQueue,
                    depthImage,
                    hasStencilComponent(depthFormat),
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, 1);

            var ret = new DepthResources();
            ret.depthImage = depthImage;
            ret.depthImageMemory = depthImageMemory;
            ret.depthImageView = depthImageView;

            return ret;
        }
    }
}
