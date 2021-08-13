package com.github.maeda6uiui.mechtatel.core;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDescriptorBufferInfo;
import org.lwjgl.vulkan.VkDescriptorSetAllocateInfo;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkWriteDescriptorSet;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates descriptor sets
 *
 * @author maeda
 */
class DescriptorSetsCreator {
    public static List<Long> createDescriptorSets(
            VkDevice device,
            int numSwapchainImages,
            long descriptorPool,
            long descriptorSetLayout,
            List<Long> uniformBuffers) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer layouts = stack.mallocLong(numSwapchainImages);
            for (int i = 0; i < layouts.capacity(); i++) {
                layouts.put(i, descriptorSetLayout);
            }

            VkDescriptorSetAllocateInfo allocInfo = VkDescriptorSetAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
            allocInfo.descriptorPool(descriptorPool);
            allocInfo.pSetLayouts(layouts);

            LongBuffer pDescriptorSets = stack.mallocLong(numSwapchainImages);
            if (vkAllocateDescriptorSets(device, allocInfo, pDescriptorSets) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate descriptor sets");
            }

            var descriptorSets = new ArrayList<Long>(pDescriptorSets.capacity());

            VkDescriptorBufferInfo.Buffer bufferInfo = VkDescriptorBufferInfo.callocStack(1, stack);
            bufferInfo.offset(0);
            bufferInfo.range(UniformBufferObject.SIZEOF);

            VkWriteDescriptorSet.Buffer descriptorWrite = VkWriteDescriptorSet.callocStack(1, stack);
            descriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            descriptorWrite.dstBinding(0);
            descriptorWrite.dstArrayElement(0);
            descriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            descriptorWrite.descriptorCount(1);
            descriptorWrite.pBufferInfo(bufferInfo);

            for (int i = 0; i < pDescriptorSets.capacity(); i++) {
                long descriptorSet = pDescriptorSets.get(i);

                bufferInfo.buffer(uniformBuffers.get(i));
                descriptorWrite.dstSet(descriptorSet);
                vkUpdateDescriptorSets(device, descriptorWrite, null);

                descriptorSets.add(descriptorSet);
            }

            return descriptorSets;
        }
    }
}
