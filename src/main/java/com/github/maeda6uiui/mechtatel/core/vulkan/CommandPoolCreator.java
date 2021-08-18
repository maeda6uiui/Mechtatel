package com.github.maeda6uiui.mechtatel.core.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates command pools
 *
 * @author maeda
 */
class CommandPoolCreator {
    public static long createCommandPool(VkDevice device, long surface) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            QueueFamilyIndices queueFamilyIndices = QueueFamilyUtils.findQueueFamilies(device.getPhysicalDevice(), surface);

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.callocStack(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolInfo.queueFamilyIndex(queueFamilyIndices.graphicsFamily);

            LongBuffer pCommandPool = stack.mallocLong(1);
            if (vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a command pool");
            }

            return pCommandPool.get(0);
        }
    }
}
