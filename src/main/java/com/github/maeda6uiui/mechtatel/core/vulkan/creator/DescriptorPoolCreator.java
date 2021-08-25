package com.github.maeda6uiui.mechtatel.core.vulkan.creator;

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
public class DescriptorPoolCreator {
    public static long createDescriptorPool(VkDevice device, int numSwapchainImages) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDescriptorPoolSize.Buffer poolSizes = VkDescriptorPoolSize.callocStack(2, stack);

            VkDescriptorPoolSize uniformBufferPoolSize = poolSizes.get(0);
            uniformBufferPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uniformBufferPoolSize.descriptorCount(numSwapchainImages);

            VkDescriptorPoolSize textureSamplerPoolSize = poolSizes.get(1);
            textureSamplerPoolSize.type(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            textureSamplerPoolSize.descriptorCount(numSwapchainImages);

            VkDescriptorPoolCreateInfo poolInfo = VkDescriptorPoolCreateInfo.callocStack(stack);
            poolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            poolInfo.pPoolSizes(poolSizes);
            poolInfo.maxSets(numSwapchainImages);

            LongBuffer pDescriptorPool = stack.mallocLong(1);
            if (vkCreateDescriptorPool(device, poolInfo, null, pDescriptorPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a descriptor pool");
            }

            return pDescriptorPool.get(0);
        }
    }
}
