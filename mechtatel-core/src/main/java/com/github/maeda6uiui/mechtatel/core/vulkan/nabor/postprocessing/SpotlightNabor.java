package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.LightingInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.SpotlightUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.net.URL;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Nabor for spotlights
 *
 * @author maeda6uiui
 */
public class SpotlightNabor extends PostProcessingNabor {
    public static final int MAX_NUM_LIGHTS = 64;

    public SpotlightNabor(VkDevice device, URL vertShaderResource, URL fragShaderResource) {
        super(device, vertShaderResource, fragShaderResource);
    }

    @Override
    protected void createUniformBuffers(int descriptorCount) {
        VkDevice device = this.getDevice();

        var cameraUBOInfos = BufferUtils.createUBOBuffers(
                device, descriptorCount, CameraUBO.SIZEOF);
        for (var cameraUBOInfo : cameraUBOInfos) {
            this.getUniformBuffers().add(cameraUBOInfo.buffer);
            this.getUniformBufferMemories().add(cameraUBOInfo.bufferMemory);
        }

        var lightingInfoUBOInfos = BufferUtils.createUBOBuffers(
                device, descriptorCount, LightingInfoUBO.SIZEOF);
        for (var lightingInfoUBOInfo : lightingInfoUBOInfos) {
            this.getUniformBuffers().add(lightingInfoUBOInfo.buffer);
            this.getUniformBufferMemories().add(lightingInfoUBOInfo.bufferMemory);
        }

        var lightUBOInfos = BufferUtils.createUBOBuffers(
                device, descriptorCount, SpotlightUBO.SIZEOF * MAX_NUM_LIGHTS);
        for (var lightUBOInfo : lightUBOInfos) {
            this.getUniformBuffers().add(lightUBOInfo.buffer);
            this.getUniformBufferMemories().add(lightUBOInfo.bufferMemory);
        }
    }

    @Override
    protected void createDescriptorSetLayoutSet0() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            VkDescriptorSetLayoutBinding.Buffer uboLayoutBindings = VkDescriptorSetLayoutBinding.calloc(3, stack);

            VkDescriptorSetLayoutBinding cameraUBOLayoutBinding = uboLayoutBindings.get(0);
            cameraUBOLayoutBinding.binding(0);
            cameraUBOLayoutBinding.descriptorCount(1);
            cameraUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBOLayoutBinding.pImmutableSamplers(null);
            cameraUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding lightingInfoUBOLayoutBinding = uboLayoutBindings.get(1);
            lightingInfoUBOLayoutBinding.binding(1);
            lightingInfoUBOLayoutBinding.descriptorCount(1);
            lightingInfoUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            lightingInfoUBOLayoutBinding.pImmutableSamplers(null);
            lightingInfoUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding lightUBOLayoutBinding = uboLayoutBindings.get(2);
            lightUBOLayoutBinding.binding(2);
            lightUBOLayoutBinding.descriptorCount(1);
            lightUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            lightUBOLayoutBinding.pImmutableSamplers(null);
            lightUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutCreateInfo.Buffer layoutCreateInfos = VkDescriptorSetLayoutCreateInfo.calloc(1, stack);

            VkDescriptorSetLayoutCreateInfo uboLayoutCreateInfo = layoutCreateInfos.get(0);
            uboLayoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            uboLayoutCreateInfo.pBindings(uboLayoutBindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            if (vkCreateDescriptorSetLayout(device, layoutCreateInfos.get(0), null, pDescriptorSetLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a descriptor set layout");
            }

            long descriptorSetLayout = pDescriptorSetLayout.get(0);
            this.getDescriptorSetLayouts().add(descriptorSetLayout);
        }
    }

    @Override
    protected void createDescriptorPoolSet0(int descriptorCount) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.calloc(3, stack);

            VkDescriptorPoolSize cameraUBOPoolSize = uboPoolSizes.get(0);
            cameraUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBOPoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize lightingInfoUBOPoolSize = uboPoolSizes.get(1);
            lightingInfoUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            lightingInfoUBOPoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize lightUBOPoolSize = uboPoolSizes.get(2);
            lightUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            lightUBOPoolSize.descriptorCount(descriptorCount * MAX_NUM_LIGHTS);

            VkDescriptorPoolCreateInfo.Buffer poolInfos = VkDescriptorPoolCreateInfo.calloc(1, stack);

            VkDescriptorPoolCreateInfo uboPoolInfo = poolInfos.get(0);
            uboPoolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            uboPoolInfo.pPoolSizes(uboPoolSizes);
            uboPoolInfo.maxSets(descriptorCount);

            LongBuffer pDescriptorPool = stack.mallocLong(1);
            if (vkCreateDescriptorPool(device, poolInfos.get(0), null, pDescriptorPool) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a descriptor pool");
            }

            long descriptorPool = pDescriptorPool.get(0);
            this.getDescriptorPools().add(descriptorPool);
        }
    }

    @Override
    protected void updateDescriptorSet0(int descriptorCount, long commandPool, VkQueue graphicsQueue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.calloc(1, stack);

            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.calloc(3, stack);

            VkDescriptorBufferInfo cameraUBOInfo = uboInfos.get(0);
            cameraUBOInfo.buffer(this.getUniformBuffer(0));
            cameraUBOInfo.offset(0);
            cameraUBOInfo.range(CameraUBO.SIZEOF);

            VkDescriptorBufferInfo lightingInfoUBOInfo = uboInfos.get(1);
            lightingInfoUBOInfo.buffer(this.getUniformBuffer(1));
            lightingInfoUBOInfo.offset(0);
            lightingInfoUBOInfo.range(LightingInfoUBO.SIZEOF);

            VkDescriptorBufferInfo lightUBOInfo = uboInfos.get(2);
            lightUBOInfo.buffer(this.getUniformBuffer(2));
            lightUBOInfo.offset(0);
            lightUBOInfo.range(SpotlightUBO.SIZEOF * MAX_NUM_LIGHTS);

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(3);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            List<Long> descriptorSets = this.getDescriptorSets();
            for (int i = 0; i < descriptorCount; i++) {
                uboDescriptorWrite.dstSet(descriptorSets.get(i));
                vkUpdateDescriptorSets(device, descriptorWrites, null);
            }
        }
    }
}
