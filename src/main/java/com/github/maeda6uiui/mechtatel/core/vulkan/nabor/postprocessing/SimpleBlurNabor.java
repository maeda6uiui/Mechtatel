package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SimpleBlurInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ShaderSPIRVUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Nabor for simple blur
 *
 * @author maeda6uiui
 */
public class SimpleBlurNabor extends PostProcessingNabor {
    public SimpleBlurNabor(VkDevice device) {
        super(
                device,
                VK_SAMPLE_COUNT_1_BIT,
                false,
                "./Mechtatel/Standard/Shader/PostProcessing/post_processing.vert",
                "./Mechtatel/Standard/Shader/PostProcessing/simple_blur.frag"
        );
    }

    @Override
    protected void createUniformBuffers(int descriptorCount) {
        VkDevice device = this.getDevice();

        var uboInfos = BufferCreator.createUBOBuffers(
                device, descriptorCount, SimpleBlurInfoUBO.SIZEOF);
        for (var uboInfo : uboInfos) {
            this.getUniformBuffers().add(uboInfo.buffer);
            this.getUniformBufferMemories().add(uboInfo.bufferMemory);
        }
    }

    @Override
    protected void createDescriptorSetLayoutSet0() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            VkDescriptorSetLayoutBinding.Buffer uboBindings = VkDescriptorSetLayoutBinding.calloc(1, stack);

            VkDescriptorSetLayoutBinding blurInfoUBOLayoutBinding = uboBindings.get(0);
            blurInfoUBOLayoutBinding.binding(0);
            blurInfoUBOLayoutBinding.descriptorCount(1);
            blurInfoUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            blurInfoUBOLayoutBinding.pImmutableSamplers(null);
            blurInfoUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutCreateInfo.Buffer layoutInfos = VkDescriptorSetLayoutCreateInfo.calloc(1, stack);

            VkDescriptorSetLayoutCreateInfo uboLayoutInfo = layoutInfos.get(0);
            uboLayoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            uboLayoutInfo.pBindings(uboBindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            if (vkCreateDescriptorSetLayout(device, layoutInfos.get(0), null, pDescriptorSetLayout) != VK_SUCCESS) {
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

            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.calloc(1, stack);

            VkDescriptorPoolSize blurInfoUBOPoolSize = uboPoolSizes.get(0);
            blurInfoUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            blurInfoUBOPoolSize.descriptorCount(descriptorCount);

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

            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.calloc(1, stack);

            VkDescriptorBufferInfo blurUBOInfo = uboInfos.get(0);
            blurUBOInfo.buffer(this.getUniformBuffer(0));
            blurUBOInfo.offset(0);
            blurUBOInfo.range(SimpleBlurInfoUBO.SIZEOF);

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(1);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            List<Long> descriptorSets = this.getDescriptorSets();
            for (int i = 0; i < descriptorCount; i++) {
                uboDescriptorWrite.dstSet(descriptorSets.get(i));
                vkUpdateDescriptorSets(device, descriptorWrites, null);
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
            String vertShaderFilepath = this.getVertShaderFilepath();
            String fragShaderFilepath = this.getFragShaderFilepath();

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

            this.addVertShaderModule(vertShaderModule);
            this.addFragShaderModule(fragShaderModule);
        }

        this.createGraphicsPipelines(vertShaderModule, fragShaderModule);
    }
}
