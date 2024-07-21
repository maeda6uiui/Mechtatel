package com.github.maeda6uiui.mechtatel.core.vulkan;

import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

/**
 * VkDevice and VkQueue
 *
 * @author maeda6uiui
 */
public record DeviceAndQueues(
        VkDevice device,
        VkQueue graphicsQueue,
        VkQueue presentQueue,
        int graphicsFamilyIndex,
        int presentFamilyIndex) {
}
