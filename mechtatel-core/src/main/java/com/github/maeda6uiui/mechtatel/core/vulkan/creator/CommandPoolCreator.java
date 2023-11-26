package com.github.maeda6uiui.mechtatel.core.vulkan.creator;

import com.github.maeda6uiui.mechtatel.core.vulkan.util.QueueFamilyUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandPoolCreateInfo;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates command pools
 *
 * @author maeda6uiui
 */
public class CommandPoolCreator {
    public static long createCommandPool(VkDevice device, long surface) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            QueueFamilyUtils.QueueFamilyIndices queueFamilyIndices
                    = QueueFamilyUtils.findQueueFamilies(device.getPhysicalDevice(), surface);

            VkCommandPoolCreateInfo poolInfo = VkCommandPoolCreateInfo.calloc(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_COMMAND_POOL_CREATE_INFO);
            poolInfo.flags(VK_COMMAND_POOL_CREATE_TRANSIENT_BIT);
            poolInfo.queueFamilyIndex(queueFamilyIndices.graphicsFamily);

            LongBuffer pCommandPool = stack.mallocLong(1);
            if (vkCreateCommandPool(device, poolInfo, null, pCommandPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a command pool");
            }

            return pCommandPool.get(0);
        }
    }
}
