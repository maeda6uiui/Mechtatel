package com.github.maeda6uiui.mechtatel.core.vulkan.util;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkInstance;
import org.lwjgl.vulkan.VkPhysicalDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.IntBuffer;
import java.util.ArrayList;

import static org.lwjgl.vulkan.VK10.vkEnumeratePhysicalDevices;

/**
 * Picks up a suitable physical device
 *
 * @author maeda6uiui
 */
public class PhysicalDevicePicker {
    private static final Logger logger = LoggerFactory.getLogger(PhysicalDevicePicker.class);

    public static VkPhysicalDevice pickPhysicalDevice(
            VkInstance instance,
            long surface,
            int preferablePhysicalDeviceIndex) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer deviceCount = stack.ints(0);
            vkEnumeratePhysicalDevices(instance, deviceCount, null);

            logger.debug("Found {} physical devices", deviceCount.get(0));
            if (deviceCount.get(0) == 0) {
                throw new RuntimeException("Failed to find GPUs with Vulkan support");
            }

            PointerBuffer ppPhysicalDevices = stack.mallocPointer(deviceCount.get(0));
            vkEnumeratePhysicalDevices(instance, deviceCount, ppPhysicalDevices);

            var devices = new ArrayList<VkPhysicalDevice>();
            for (int i = 0; i < ppPhysicalDevices.capacity(); i++) {
                var device = new VkPhysicalDevice(ppPhysicalDevices.get(i), instance);
                if (QueueFamilyUtils.isDeviceSuitable(device, surface, SwapchainUtils.DEVICE_EXTENSIONS)) {
                    devices.add(device);
                }
            }
            logger.debug("Found {} suitable physical devices", devices.size());

            if (devices.isEmpty()) {
                throw new RuntimeException("Failed to find a suitable GPU");
            } else if (preferablePhysicalDeviceIndex == -1) {
                return devices.get(devices.size() - 1);
            } else if (preferablePhysicalDeviceIndex >= 0 && preferablePhysicalDeviceIndex < devices.size()) {
                return devices.get(preferablePhysicalDeviceIndex);
            } else {
                logger.warn(
                        "preferablePhysicalDeviceIndex ({}) is out of range, return first suitable physical device",
                        preferablePhysicalDeviceIndex);
                return devices.get(0);
            }
        }
    }
}
