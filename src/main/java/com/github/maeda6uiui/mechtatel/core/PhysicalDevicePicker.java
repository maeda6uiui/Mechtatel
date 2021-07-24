package com.github.maeda6uiui.mechtatel.core;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;

import java.nio.IntBuffer;

import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;

/**
 * Picks up suitable physical devices
 *
 * @author maeda
 */
class PhysicalDevicePicker {
    public static VkPhysicalDevice pickPhysicalDevice(VkInstance instance, long surface) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer deviceCount = stack.ints(0);
            vkEnumeratePhysicalDevices(instance, deviceCount, null);

            if (deviceCount.get(0) == 0) {
                throw new RuntimeException("Failed to find GPUs with Vulkan support");
            }

            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));
            vkEnumeratePhysicalDevices(instance, deviceCount, ppPhysicalDevices);

            for (int i = 0; i < ppPhysicalDevices.capacity(); i++) {
                var device = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);
                if (QueueFamilyMethods.isDeviceSuitable(device, surface)) {
                    return device;
                }
            }

            throw new RuntimeException("Failed to find a suitable GPU");
        }
    }
}
