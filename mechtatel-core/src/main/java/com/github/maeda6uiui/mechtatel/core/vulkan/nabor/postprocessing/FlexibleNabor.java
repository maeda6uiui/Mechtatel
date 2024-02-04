package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.nabor.FlexibleNaborInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.net.URL;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Flexible nabor<br>
 * This nabor can be used with user-defined external shaders
 * if the post processing is achieved with a single fragment shader.
 * You have to create your own nabor if the processing
 * requires more than one pair of shaders (e.g. shadow mapping).
 * Plus, you can't use more than one lighting types at the same time
 * in the fragment shader consumed by this nabor.
 *
 * @author maeda6uiui
 */
public class FlexibleNabor extends PostProcessingNabor {
    private List<FlexibleNaborInfo.UniformResourceType> uniformResourceTypes;

    public FlexibleNabor(
            VkDevice device,
            URL vertShaderResource,
            URL fragShaderResource,
            List<FlexibleNaborInfo.UniformResourceType> uniformResourceTypes) {
        super(device, VK_SAMPLE_COUNT_1_BIT, false, vertShaderResource, fragShaderResource);

        this.uniformResourceTypes = uniformResourceTypes;
    }

    @Override
    protected void createUniformBuffers(int descriptorCount) {
        VkDevice device = this.getDevice();

        for (var uniformResourceType : uniformResourceTypes) {
            int size = switch (uniformResourceType) {
                case CAMERA -> CameraUBO.SIZEOF;
                case FOG -> FogUBO.SIZEOF;
                case LIGHTING_INFO -> LightingInfoUBO.SIZEOF;
                case PARALLEL_LIGHT -> ParallelLightUBO.SIZEOF;
                case POINT_LIGHT -> PointLightUBO.SIZEOF;
                case SIMPLE_BLUR -> SimpleBlurInfoUBO.SIZEOF;
                case SPOTLIGHT -> SpotlightUBO.SIZEOF;
            };
            var uboInfos = BufferUtils.createUBOBuffers(device, descriptorCount, size);
            for (var uboInfo : uboInfos) {
                this.getUniformBuffers().add(uboInfo.buffer);
                this.getUniformBufferMemories().add(uboInfo.bufferMemory);
            }
        }
    }

    @Override
    protected void createDescriptorSetLayoutSet0() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            int numUniformResourceTypes = uniformResourceTypes.size();
            VkDescriptorSetLayoutBinding.Buffer uboLayoutBindings
                    = VkDescriptorSetLayoutBinding.calloc(numUniformResourceTypes, stack);

            for (int i = 0; i < numUniformResourceTypes; i++) {
                VkDescriptorSetLayoutBinding uboLayoutBinding = uboLayoutBindings.get(i);
                uboLayoutBinding.binding(i);
                uboLayoutBinding.descriptorCount(1);
                uboLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                uboLayoutBinding.pImmutableSamplers(null);
                uboLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            }

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

            int numUniformResourceTypes = uniformResourceTypes.size();
            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.calloc(numUniformResourceTypes, stack);

            for (int i = 0; i < numUniformResourceTypes; i++) {
                VkDescriptorPoolSize uboPoolSize = uboPoolSizes.get(i);
                uboPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                uboPoolSize.descriptorCount(descriptorCount);
            }

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

            int numUniformResourceTypes = uniformResourceTypes.size();
            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.calloc(numUniformResourceTypes, stack);

            for (int i = 0; i < numUniformResourceTypes; i++) {
                VkDescriptorBufferInfo uboInfo = uboInfos.get(i);
                uboInfo.buffer(this.getUniformBuffer(i));
                uboInfo.offset(0);

                switch (uniformResourceTypes.get(i)) {
                    case CAMERA -> uboInfo.range(CameraUBO.SIZEOF);
                    case FOG -> uboInfo.range(FogUBO.SIZEOF);
                    case LIGHTING_INFO -> uboInfo.range(LightingInfoUBO.SIZEOF);
                    case PARALLEL_LIGHT -> uboInfo.range(ParallelLightUBO.SIZEOF);
                    case POINT_LIGHT -> uboInfo.range(PointLightUBO.SIZEOF);
                    case SIMPLE_BLUR -> uboInfo.range(SimpleBlurInfoUBO.SIZEOF);
                    case SPOTLIGHT -> uboInfo.range(SpotlightUBO.SIZEOF);
                }
            }

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(numUniformResourceTypes);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            List<Long> descriptorSets = this.getDescriptorSets();
            for (int i = 0; i < descriptorCount; i++) {
                uboDescriptorWrite.dstSet(descriptorSets.get(i));
                vkUpdateDescriptorSets(device, descriptorWrites, null);
            }
        }
    }
}
