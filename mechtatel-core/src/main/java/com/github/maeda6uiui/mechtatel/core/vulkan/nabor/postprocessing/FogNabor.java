package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.FogUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.net.URL;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Nabor for fog
 *
 * @author maeda6uiui
 */
public class FogNabor extends PostProcessingNabor {
    public FogNabor(VkDevice device, List<URL> vertShaderResources, List<URL> fragShaderResources) {
        super(device, vertShaderResources, fragShaderResources);
    }

    @Deprecated
    public FogNabor(VkDevice device, URL vertShaderResource, URL fragShaderResource) {
        this(device, List.of(vertShaderResource), List.of(fragShaderResource));
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

        var fogUBOInfos = BufferUtils.createUBOBuffers(
                device, descriptorCount, FogUBO.SIZEOF);
        for (var fogUBOInfo : fogUBOInfos) {
            this.getUniformBuffers().add(fogUBOInfo.buffer);
            this.getUniformBufferMemories().add(fogUBOInfo.bufferMemory);
        }
    }

    @Override
    protected void createDescriptorSetLayoutSet0() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            VkDescriptorSetLayoutBinding.Buffer uboLayoutBindings = VkDescriptorSetLayoutBinding.calloc(2, stack);

            VkDescriptorSetLayoutBinding cameraUBOLayoutBinding = uboLayoutBindings.get(0);
            cameraUBOLayoutBinding.binding(0);
            cameraUBOLayoutBinding.descriptorCount(1);
            cameraUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBOLayoutBinding.pImmutableSamplers(null);
            cameraUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding fogUBOLayoutBinding = uboLayoutBindings.get(1);
            fogUBOLayoutBinding.binding(1);
            fogUBOLayoutBinding.descriptorCount(1);
            fogUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            fogUBOLayoutBinding.pImmutableSamplers(null);
            fogUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

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

            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.calloc(2, stack);

            VkDescriptorPoolSize cameraUBOPoolSize = uboPoolSizes.get(0);
            cameraUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBOPoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize parallelLightUBOPoolSize = uboPoolSizes.get(1);
            parallelLightUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            parallelLightUBOPoolSize.descriptorCount(descriptorCount);

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

            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.calloc(2, stack);

            VkDescriptorBufferInfo cameraUBOInfo = uboInfos.get(0);
            cameraUBOInfo.buffer(this.getUniformBuffer(0));
            cameraUBOInfo.offset(0);
            cameraUBOInfo.range(CameraUBO.SIZEOF);

            VkDescriptorBufferInfo fogUBOInfo = uboInfos.get(1);
            fogUBOInfo.buffer(this.getUniformBuffer(1));
            fogUBOInfo.offset(0);
            fogUBOInfo.range(FogUBO.SIZEOF);

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(2);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            List<Long> descriptorSets = this.getDescriptorSets();
            for (int i = 0; i < descriptorCount; i++) {
                uboDescriptorWrite.dstSet(descriptorSets.get(i));
                vkUpdateDescriptorSets(device, descriptorWrites, null);
            }
        }
    }
}
