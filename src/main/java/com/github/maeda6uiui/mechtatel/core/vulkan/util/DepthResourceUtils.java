package com.github.maeda6uiui.mechtatel.core.vulkan.util;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFormatProperties;

import java.nio.IntBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Utility methods for depth resources
 *
 * @author maeda6uiui
 */
public class DepthResourceUtils {
    public static int findSupportedFormat(VkDevice device, IntBuffer formatCandidates, int tiling, int features) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFormatProperties props = VkFormatProperties.calloc(stack);

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
}
