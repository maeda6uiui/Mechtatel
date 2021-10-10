package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.LightingInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.SpotlightUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ShaderSPIRVUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Nabor for spotlights
 *
 * @author maeda
 */
public class SpotlightNabor extends PostProcessingNabor {
    public static final int MAX_NUM_LIGHTS = 64;

    public SpotlightNabor(VkDevice device) {
        super(device, VK_SAMPLE_COUNT_1_BIT);
    }

    @Override
    protected void createUniformBuffers(int descriptorCount) {
        VkDevice device = this.getDevice();

        var cameraUBOInfos = BufferCreator.createUBOBuffers(
                device, descriptorCount, CameraUBO.SIZEOF);
        for (var cameraUBOInfo : cameraUBOInfos) {
            this.getUniformBuffers().add(cameraUBOInfo.buffer);
            this.getUniformBufferMemories().add(cameraUBOInfo.bufferMemory);
        }

        var lightingInfoUBOInfos = BufferCreator.createUBOBuffers(
                device, descriptorCount, LightingInfoUBO.SIZEOF);
        for (var lightingInfoUBOInfo : lightingInfoUBOInfos) {
            this.getUniformBuffers().add(lightingInfoUBOInfo.buffer);
            this.getUniformBufferMemories().add(lightingInfoUBOInfo.bufferMemory);
        }

        var lightUBOInfos = BufferCreator.createUBOBuffers(
                device, descriptorCount, SpotlightUBO.SIZEOF * MAX_NUM_LIGHTS);
        for (var lightUBOInfo : lightUBOInfos) {
            this.getUniformBuffers().add(lightUBOInfo.buffer);
            this.getUniformBufferMemories().add(lightUBOInfo.bufferMemory);
        }
    }

    @Override
    protected void createDescriptorSetLayouts() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            //=== set 0 ===
            VkDescriptorSetLayoutBinding.Buffer uboBindings = VkDescriptorSetLayoutBinding.callocStack(3, stack);

            VkDescriptorSetLayoutBinding cameraUBOLayoutBinding = uboBindings.get(0);
            cameraUBOLayoutBinding.binding(0);
            cameraUBOLayoutBinding.descriptorCount(1);
            cameraUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBOLayoutBinding.pImmutableSamplers(null);
            cameraUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding lightingInfoUBOLayoutBinding = uboBindings.get(1);
            lightingInfoUBOLayoutBinding.binding(1);
            lightingInfoUBOLayoutBinding.descriptorCount(1);
            lightingInfoUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            lightingInfoUBOLayoutBinding.pImmutableSamplers(null);
            lightingInfoUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding lightUBOLayoutBinding = uboBindings.get(2);
            lightUBOLayoutBinding.binding(2);
            lightUBOLayoutBinding.descriptorCount(MAX_NUM_LIGHTS);
            lightUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            lightUBOLayoutBinding.pImmutableSamplers(null);
            lightUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //=== set 1 ===
            VkDescriptorSetLayoutBinding.Buffer imageBindings = VkDescriptorSetLayoutBinding.callocStack(4, stack);

            VkDescriptorSetLayoutBinding albedoImageLayoutBinding = imageBindings.get(0);
            albedoImageLayoutBinding.binding(0);
            albedoImageLayoutBinding.descriptorCount(1);
            albedoImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            albedoImageLayoutBinding.pImmutableSamplers(null);
            albedoImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding depthImageLayoutBinding = imageBindings.get(1);
            depthImageLayoutBinding.binding(1);
            depthImageLayoutBinding.descriptorCount(1);
            depthImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            depthImageLayoutBinding.pImmutableSamplers(null);
            depthImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding positionImageLayoutBinding = imageBindings.get(2);
            positionImageLayoutBinding.binding(2);
            positionImageLayoutBinding.descriptorCount(1);
            positionImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            positionImageLayoutBinding.pImmutableSamplers(null);
            positionImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding normalImageLayoutBinding = imageBindings.get(3);
            normalImageLayoutBinding.binding(3);
            normalImageLayoutBinding.descriptorCount(1);
            normalImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            normalImageLayoutBinding.pImmutableSamplers(null);
            normalImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //=== set 2 ===
            VkDescriptorSetLayoutBinding.Buffer samplerBindings = VkDescriptorSetLayoutBinding.callocStack(1, stack);

            VkDescriptorSetLayoutBinding samplerLayoutBinding = samplerBindings.get(0);
            samplerLayoutBinding.binding(0);
            samplerLayoutBinding.descriptorCount(1);
            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLER);
            samplerLayoutBinding.pImmutableSamplers(null);
            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //Create descriptor set layouts
            VkDescriptorSetLayoutCreateInfo.Buffer layoutInfos = VkDescriptorSetLayoutCreateInfo.callocStack(3, stack);

            //=== set 0 ===
            VkDescriptorSetLayoutCreateInfo uboLayoutInfo = layoutInfos.get(0);
            uboLayoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            uboLayoutInfo.pBindings(uboBindings);

            //=== set 1 ===
            VkDescriptorSetLayoutCreateInfo imageLayoutInfo = layoutInfos.get(1);
            imageLayoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            imageLayoutInfo.pBindings(imageBindings);

            //=== set 2 ===
            VkDescriptorSetLayoutCreateInfo samplerLayoutInfo = layoutInfos.get(2);
            samplerLayoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            samplerLayoutInfo.pBindings(samplerBindings);

            for (int i = 0; i < 3; i++) {
                LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
                if (vkCreateDescriptorSetLayout(device, layoutInfos.get(i), null, pDescriptorSetLayout) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create a descriptor set layout");
                }

                long descriptorSetLayout = pDescriptorSetLayout.get(0);
                this.getDescriptorSetLayouts().add(descriptorSetLayout);
            }
        }
    }

    @Override
    protected void createDescriptorPools(int descriptorCount) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            //=== set 0 ===
            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.callocStack(3, stack);

            VkDescriptorPoolSize cameraUBOPoolSize = uboPoolSizes.get(0);
            cameraUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBOPoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize lightingInfoUBOPoolSize = uboPoolSizes.get(1);
            lightingInfoUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            lightingInfoUBOPoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize lightUBOPoolSize = uboPoolSizes.get(2);
            lightUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            lightUBOPoolSize.descriptorCount(descriptorCount * MAX_NUM_LIGHTS);

            //=== set 1 ===
            VkDescriptorPoolSize.Buffer imagePoolSizes = VkDescriptorPoolSize.callocStack(4, stack);

            VkDescriptorPoolSize albedoImagePoolSize = imagePoolSizes.get(0);
            albedoImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            albedoImagePoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize depthImagePoolSize = imagePoolSizes.get(1);
            depthImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            depthImagePoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize positionImagePoolSize = imagePoolSizes.get(2);
            positionImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            positionImagePoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize normalImagePoolSize = imagePoolSizes.get(3);
            normalImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            normalImagePoolSize.descriptorCount(descriptorCount);

            //=== set 2 ===
            VkDescriptorPoolSize.Buffer samplerPoolSizes = VkDescriptorPoolSize.callocStack(1, stack);

            VkDescriptorPoolSize samplerPoolSize = samplerPoolSizes.get(0);
            samplerPoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLER);
            samplerPoolSize.descriptorCount(descriptorCount);

            //Create descriptor pools
            VkDescriptorPoolCreateInfo.Buffer poolInfos = VkDescriptorPoolCreateInfo.callocStack(3, stack);

            //=== set 0 ===
            VkDescriptorPoolCreateInfo uboPoolInfo = poolInfos.get(0);
            uboPoolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            uboPoolInfo.pPoolSizes(uboPoolSizes);
            uboPoolInfo.maxSets(descriptorCount);

            //=== set 1 ===
            VkDescriptorPoolCreateInfo imagePoolInfo = poolInfos.get(1);
            imagePoolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            imagePoolInfo.pPoolSizes(imagePoolSizes);
            imagePoolInfo.maxSets(descriptorCount);

            //=== set 2 ===
            VkDescriptorPoolCreateInfo samplerPoolInfo = poolInfos.get(2);
            samplerPoolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            samplerPoolInfo.pPoolSizes(samplerPoolSizes);
            samplerPoolInfo.maxSets(descriptorCount);

            for (int i = 0; i < 3; i++) {
                LongBuffer pDescriptorPool = stack.mallocLong(1);
                if (vkCreateDescriptorPool(device, poolInfos.get(i), null, pDescriptorPool) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create a descriptor pool");
                }

                long descriptorPool = pDescriptorPool.get(0);
                this.getDescriptorPools().add(descriptorPool);
            }
        }
    }

    @Override
    protected void createDescriptorSets(int descriptorCount, long commandPool, VkQueue graphicsQueue) {
        super.createDescriptorSets(descriptorCount, commandPool, graphicsQueue);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            List<Long> descriptorSetLayouts = this.getDescriptorSetLayouts();
            List<Long> descriptorPools = this.getDescriptorPools();
            List<Long> descriptorSets = new ArrayList<>();

            int setCount = descriptorSetLayouts.size();
            this.setSetCount(setCount);

            VkDescriptorSetAllocateInfo.Buffer allocInfos = VkDescriptorSetAllocateInfo.callocStack(setCount, stack);

            for (int i = 0; i < setCount; i++) {
                LongBuffer layouts = stack.mallocLong(descriptorCount);

                for (int j = 0; j < descriptorCount; j++) {
                    layouts.put(j, descriptorSetLayouts.get(i));
                }

                VkDescriptorSetAllocateInfo allocInfo = allocInfos.get(i);
                allocInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_ALLOCATE_INFO);
                allocInfo.descriptorPool(descriptorPools.get(i));
                allocInfo.pSetLayouts(layouts);

                LongBuffer pDescriptorSets = stack.mallocLong(descriptorCount);
                if (vkAllocateDescriptorSets(device, allocInfo, pDescriptorSets) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to allocate descriptor sets");
                }

                for (int j = 0; j < descriptorCount; j++) {
                    descriptorSets.add(pDescriptorSets.get(j));
                }
            }

            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.callocStack(setCount, stack);

            //=== set 0 ===
            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.callocStack(2, stack);

            VkDescriptorBufferInfo cameraUBOInfo = uboInfos.get(0);
            cameraUBOInfo.buffer(this.getUniformBuffer(0));
            cameraUBOInfo.offset(0);
            cameraUBOInfo.range(CameraUBO.SIZEOF);

            VkDescriptorBufferInfo lightingInfoUBOInfo = uboInfos.get(1);
            lightingInfoUBOInfo.buffer(this.getUniformBuffer(1));
            lightingInfoUBOInfo.offset(0);
            lightingInfoUBOInfo.range(LightingInfoUBO.SIZEOF);

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(2);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            VkWriteDescriptorSet.Buffer lightUBODescriptorWrite = VkWriteDescriptorSet.callocStack(1, stack);
            VkDescriptorBufferInfo.Buffer lightUBOInfos = VkDescriptorBufferInfo.callocStack(MAX_NUM_LIGHTS, stack);
            for (int i = 0; i < MAX_NUM_LIGHTS; i++) {
                VkDescriptorBufferInfo lightUBOInfo = lightUBOInfos.get(i);
                lightUBOInfo.buffer(this.getUniformBuffer(2));
                lightUBOInfo.offset(0);
                lightUBOInfo.range(SpotlightUBO.SIZEOF);
            }

            lightUBODescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            lightUBODescriptorWrite.dstBinding(2);
            lightUBODescriptorWrite.dstArrayElement(0);
            lightUBODescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            lightUBODescriptorWrite.descriptorCount(MAX_NUM_LIGHTS);
            lightUBODescriptorWrite.pBufferInfo(lightUBOInfos);

            //=== set 1 ===
            VkDescriptorImageInfo.Buffer imageInfos = VkDescriptorImageInfo.callocStack(4, stack);
            for (int i = 0; i < 4; i++) {
                VkDescriptorImageInfo imageInfo = imageInfos.get(i);
                imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                imageInfo.imageView(this.getDummyImageView());
            }

            VkWriteDescriptorSet imageDescriptorWrite = descriptorWrites.get(1);
            imageDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            imageDescriptorWrite.dstBinding(0);
            imageDescriptorWrite.dstArrayElement(0);
            imageDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            imageDescriptorWrite.descriptorCount(4);
            imageDescriptorWrite.pImageInfo(imageInfos);

            //=== set 2 ===
            VkDescriptorImageInfo.Buffer samplerInfos = VkDescriptorImageInfo.callocStack(1, stack);

            VkDescriptorImageInfo samplerInfo = samplerInfos.get(0);
            samplerInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            samplerInfo.sampler(this.getTextureSampler(0));

            VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(2);
            samplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            samplerDescriptorWrite.dstBinding(0);
            samplerDescriptorWrite.dstArrayElement(0);
            samplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLER);
            samplerDescriptorWrite.descriptorCount(1);
            samplerDescriptorWrite.pImageInfo(samplerInfos);

            for (int i = 0; i < descriptorCount; i++) {
                uboDescriptorWrite.dstSet(descriptorSets.get(i));
                imageDescriptorWrite.dstSet(descriptorSets.get(i + descriptorCount));
                samplerDescriptorWrite.dstSet(descriptorSets.get(i + descriptorCount * 2));
                vkUpdateDescriptorSets(device, descriptorWrites, null);

                lightUBODescriptorWrite.dstSet(descriptorSets.get(i));
                vkUpdateDescriptorSets(device, lightUBODescriptorWrite, null);
            }

            for (int i = 0; i < descriptorSets.size(); i++) {
                this.getDescriptorSets().add(descriptorSets.get(i));
            }
        }
    }

    @Override
    protected void createGraphicsPipelines() {
        VkDevice device = this.getDevice();

        long vertShaderModule;
        long fragShaderModule;
        if (this.getVertShaderModules().size() != 0) {
            vertShaderModule = this.getVertShaderModule(0);
            fragShaderModule = this.getFragShaderModule(0);
        } else {
            final String vertShaderFilepath = "./Mechtatel/Shader/Standard/PostProcessing/post_processing.vert";
            final String fragShaderFilepath = "./Mechtatel/Shader/Standard/PostProcessing/spotlight.frag";

            ShaderSPIRVUtils.SPIRV vertShaderSPIRV;
            ShaderSPIRVUtils.SPIRV fragShaderSPIRV;
            try {
                vertShaderSPIRV = ShaderSPIRVUtils.compileShaderFile(vertShaderFilepath, ShaderSPIRVUtils.ShaderKind.VERTEX_SHADER);
                fragShaderSPIRV = ShaderSPIRVUtils.compileShaderFile(fragShaderFilepath, ShaderSPIRVUtils.ShaderKind.FRAGMENT_SHADER);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            vertShaderModule = this.createShaderModule(device, vertShaderSPIRV.bytecode());
            fragShaderModule = this.createShaderModule(device, fragShaderSPIRV.bytecode());

            this.getVertShaderModules().add(vertShaderModule);
            this.getFragShaderModules().add(fragShaderModule);
        }

        this.createGraphicsPipelines(vertShaderModule, fragShaderModule);
    }
}
