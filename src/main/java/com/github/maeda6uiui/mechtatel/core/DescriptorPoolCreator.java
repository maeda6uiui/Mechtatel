package com.github.maeda6uiui.mechtatel.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorPoolCreateInfo;
import org.lwjgl.vulkan.VkDescriptorPoolSize;
import org.lwjgl.vulkan.VkDevice;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates descriptor pools
 *
 * @author maeda
 */
class DescriptorPoolCreator {
    public static long createDescriptorPool(VkDevice device, int numSwapchainImages) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorPoolSize.Buffer poolSize = VkDescriptorPoolSize.callocStack(1, stack);
            poolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            poolSize.descriptorCount(numSwapchainImages);

            VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            poolInfo.pPoolSizes(poolSize);
            poolInfo.maxSets(numSwapchainImages);

            LongBuffer pDescriptorPool = stack.mallocLong(1);
            if (vkCreateDescriptorPool(device, poolInfo, null, pDescriptorPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a descriptor pool");
            }

            return pDescriptorPool.get(0);
        }
    }
}
