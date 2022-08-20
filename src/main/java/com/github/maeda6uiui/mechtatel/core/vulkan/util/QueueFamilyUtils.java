package com.github.maeda6uiui.mechtatel.core.vulkan.util;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.util.Set;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Utility methods for queue families
 *
 * @author maeda
 */
public class QueueFamilyUtils {
    public static class QueueFamilyIndices {
        public Integer graphicsFamily;
        public Integer presentFamily;

        public boolean isComplete() {
            return graphicsFamily != null && presentFamily != null;
        }
    }

    public static QueueFamilyIndices findQueueFamilies(VkPhysicalDevice device, long surface) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var indices = new QueueFamilyIndices();

            IntBuffer queueFamilyCount = stack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK_FALSE);

            for (int i = 0; i < queueFamilies.capacity() || !indices.isComplete(); i++) {
                if ((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    indices.graphicsFamily = i;
                }

                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);
                if (presentSupport.get(0) == VK_TRUE) {
                    indices.presentFamily = i;
                }
            }

            return indices;
        }
    }

    private static boolean checkExtensionSupported(VkPhysicalDevice device, Set<String> deviceExtensions) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer extensionCount = stack.ints(0);
            vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, null);

            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.mallocStack(extensionCount.get(0), stack);
            vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, availableExtensions);

            return availableExtensions.stream()
                    .map(VkExtensionProperties::extensionNameString)
                    .collect(Collectors.toSet())
                    .containsAll(deviceExtensions);
        }
    }

    public static boolean isDeviceSuitable(VkPhysicalDevice device, long surface, Set<String> deviceExtensions) {
        QueueFamilyIndices indices = findQueueFamilies(device, surface);

        boolean extensionsSupported = checkExtensionSupported(device, deviceExtensions);
        boolean swapchainAdequate = false;
        boolean anisotropySupported = false;

        if (extensionsSupported) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                SwapchainUtils.SwapchainSupportDetails swapchainSupport
                        = SwapchainUtils.querySwapchainSupport(device, surface, stack);
                swapchainAdequate = swapchainSupport.formats.hasRemaining() && swapchainSupport.presentModes.hasRemaining();

                VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.mallocStack(stack);
                vkGetPhysicalDeviceFeatures(device, supportedFeatures);
                anisotropySupported = supportedFeatures.samplerAnisotropy();
            }
        }

        return indices.isComplete() && extensionsSupported && swapchainAdequate && anisotropySupported;
    }
}
