package com.github.maeda6uiui.mechtatel.core.vulkan.creator;

import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
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
public class DescriptorSetsCreator {
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

            VkDescriptorBufferInfo.Buffer cameraUBOInfo = VkDescriptorBufferInfo.callocStack(1, stack);
            cameraUBOInfo.offset(0);
            cameraUBOInfo.range(CameraUBO.SIZEOF);

            VkWriteDescriptorSet.Buffer cameraUBODescriptorWrite = VkWriteDescriptorSet.callocStack(1, stack);
            cameraUBODescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            cameraUBODescriptorWrite.dstBinding(0);
            cameraUBODescriptorWrite.dstArrayElement(0);
            cameraUBODescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBODescriptorWrite.descriptorCount(1);
            cameraUBODescriptorWrite.pBufferInfo(cameraUBOInfo);

            for (int i = 0; i < pDescriptorSets.capacity(); i++) {
                long descriptorSet = pDescriptorSets.get(i);

                cameraUBOInfo.buffer(uniformBuffers.get(i));
                cameraUBODescriptorWrite.dstSet(descriptorSet);

                vkUpdateDescriptorSets(device, cameraUBODescriptorWrite, null);

                descriptorSets.add(descriptorSet);
            }

            return descriptorSets;
        }
    }
}
