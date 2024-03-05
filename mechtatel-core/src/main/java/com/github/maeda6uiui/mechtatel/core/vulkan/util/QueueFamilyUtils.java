package com.github.maeda6uiui.mechtatel.core.vulkan.util;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Collectors;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Utility methods for queue families
 *
 * @author maeda6uiui
 */
public class QueueFamilyUtils {
    private static final Logger logger = LoggerFactory.getLogger(QueueFamilyUtils.class);

    public record QueueFamilyIndices(int graphicsFamily, int presentFamily) {
    }

    public static QueueFamilyIndices findQueueFamilies(
            VkPhysicalDevice device,
            long surface,
            int preferableGraphicsFamilyIndex,
            int preferablePresentFamilyIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer queueFamilyCount = stack.ints(0);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, null);

            VkQueueFamilyProperties.Buffer queueFamilies = VkQueueFamilyProperties.malloc(queueFamilyCount.get(0), stack);
            vkGetPhysicalDeviceQueueFamilyProperties(device, queueFamilyCount, queueFamilies);

            IntBuffer presentSupport = stack.ints(VK_FALSE);

            var graphicsFamilyCandidates = new ArrayList<Integer>();
            var presentFamilyCandidates = new ArrayList<Integer>();
            for (int i = 0; i < queueFamilies.capacity(); i++) {
                if ((queueFamilies.get(i).queueFlags() & VK_QUEUE_GRAPHICS_BIT) != 0) {
                    graphicsFamilyCandidates.add(i);
                }

                KHRSurface.vkGetPhysicalDeviceSurfaceSupportKHR(device, i, surface, presentSupport);
                if (presentSupport.get(0) == VK_TRUE) {
                    presentFamilyCandidates.add(i);
                }
            }

            logger.debug("Graphics family candidates: {}", graphicsFamilyCandidates);
            logger.debug("Present family candidates: {}", presentFamilyCandidates);

            if (graphicsFamilyCandidates.isEmpty()) {
                throw new RuntimeException("No suitable graphics family found");
            }
            if (presentFamilyCandidates.isEmpty()) {
                throw new RuntimeException("No suitable present family found");
            }

            int graphicsFamily;
            if (preferableGraphicsFamilyIndex == -1) {
                graphicsFamily = graphicsFamilyCandidates.get(graphicsFamilyCandidates.size() - 1);
            } else if (graphicsFamilyCandidates.contains(preferableGraphicsFamilyIndex)) {
                graphicsFamily = preferableGraphicsFamilyIndex;
            } else {
                graphicsFamily = graphicsFamilyCandidates.get(0);
                logger.warn("Queue family ({}) is not a suitable graphics family", preferableGraphicsFamilyIndex);
            }

            int presentFamily;
            if (preferablePresentFamilyIndex == -1) {
                presentFamily = presentFamilyCandidates.get(presentFamilyCandidates.size() - 1);
            } else if (presentFamilyCandidates.contains(preferablePresentFamilyIndex)) {
                presentFamily = preferablePresentFamilyIndex;
            } else {
                presentFamily = presentFamilyCandidates.get(0);
                logger.warn("Queue family ({}) is not a suitable present family", preferablePresentFamilyIndex);
            }

            logger.debug("graphicsFamily={} presentFamily={}", graphicsFamily, presentFamily);

            return new QueueFamilyIndices(graphicsFamily, presentFamily);
        }
    }

    private static boolean checkExtensionSupported(VkPhysicalDevice device, Set<String> deviceExtensions) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer extensionCount = stack.ints(0);
            vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, null);

            VkExtensionProperties.Buffer availableExtensions = VkExtensionProperties.malloc(extensionCount.get(0), stack);
            vkEnumerateDeviceExtensionProperties(device, (String) null, extensionCount, availableExtensions);

            return availableExtensions.stream()
                    .map(VkExtensionProperties::extensionNameString)
                    .collect(Collectors.toSet())
                    .containsAll(deviceExtensions);
        }
    }

    public static boolean isDeviceSuitable(VkPhysicalDevice device, long surface, Set<String> deviceExtensions) {
        boolean extensionsSupported = checkExtensionSupported(device, deviceExtensions);
        boolean swapchainAdequate = false;
        boolean anisotropySupported = false;

        if (extensionsSupported) {
            try (MemoryStack stack = MemoryStack.stackPush()) {
                SwapchainUtils.SwapchainSupportDetails swapchainSupport
                        = SwapchainUtils.querySwapchainSupport(device, surface, stack);
                swapchainAdequate = swapchainSupport.formats.hasRemaining() && swapchainSupport.presentModes.hasRemaining();

                VkPhysicalDeviceFeatures supportedFeatures = VkPhysicalDeviceFeatures.malloc(stack);
                vkGetPhysicalDeviceFeatures(device, supportedFeatures);
                anisotropySupported = supportedFeatures.samplerAnisotropy();
            }
        }

        return extensionsSupported && swapchainAdequate && anisotropySupported;
    }
}
