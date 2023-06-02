package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing;

import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.*;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.Pass1InfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.Pass2InfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.ShadowInfoUBO;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Flexible nabor<br>
 * This nabor can be used with user-defined external shaders
 * if the post processing is achieved with a single pair of vertex and fragment shaders.
 * You have to create your own nabor if the post processing
 * requires more than one pair of shaders (e.g. shadow mapping).
 * Plus, you can't use more than one lighting types at the same time.
 *
 * @author maeda6uiui
 */
public class FlexibleNabor extends PostProcessingNabor {
    private List<String> uniformResources;

    public FlexibleNabor(
            VkDevice device,
            String vertShaderFilepath,
            String fragShaderFilepath,
            List<String> uniformResources) {
        super(device, VK_SAMPLE_COUNT_1_BIT, false, vertShaderFilepath, fragShaderFilepath);

        this.uniformResources = uniformResources;
    }

    @Override
    protected void createUniformBuffers(int descriptorCount) {
        VkDevice device = this.getDevice();

        for (String uniformResource : uniformResources) {
            int size;
            switch (uniformResource) {
                case "camera":
                    size = CameraUBO.SIZEOF;
                    break;
                case "fog":
                    size = FogUBO.SIZEOF;
                    break;
                case "lighting_info":
                    size = LightingInfoUBO.SIZEOF;
                    break;
                case "parallel_light":
                    size = ParallelLightUBO.SIZEOF;
                    break;
                case "point_light":
                    size = PointLightUBO.SIZEOF;
                    break;
                case "simple_blur":
                    size = SimpleBlurInfoUBO.SIZEOF;
                    break;
                case "spotlight":
                    size = SpotlightUBO.SIZEOF;
                    break;
                default:
                    throw new IllegalArgumentException("Unsupported uniform resource specified: " + uniformResource);
            }

            var uboInfos = BufferCreator.createUBOBuffers(device, descriptorCount, size);
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

            int numUniformResources = uniformResources.size();
            VkDescriptorSetLayoutBinding.Buffer uboBindings = VkDescriptorSetLayoutBinding.calloc(numUniformResources, stack);

            for (int i = 0; i < numUniformResources; i++) {
                VkDescriptorSetLayoutBinding uboLayoutBinding = uboBindings.get(i);
                uboLayoutBinding.binding(i);
                uboLayoutBinding.descriptorCount(1);
                uboLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
                uboLayoutBinding.pImmutableSamplers(null);
                uboLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);
            }

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

            int numUniformResources = uniformResources.size();
            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.calloc(numUniformResources, stack);

            for (int i = 0; i < numUniformResources; i++) {
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

            int numUniformResources = uniformResources.size();
            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.calloc(numUniformResources, stack);

            for (int i = 0; i < numUniformResources; i++) {
                VkDescriptorBufferInfo uboInfo = uboInfos.get(i);
                uboInfo.buffer(this.getUniformBuffer(i));
                uboInfo.offset(0);

                String uniformResource = uniformResources.get(i);
                switch (uniformResource) {
                    case "camera":
                        uboInfo.range(CameraUBO.SIZEOF);
                        break;
                    case "fog":
                        uboInfo.range(FogUBO.SIZEOF);
                        break;
                    case "lighting_info":
                        uboInfo.range(LightingInfoUBO.SIZEOF);
                        break;
                    case "parallel_light":
                        uboInfo.range(ParallelLightUBO.SIZEOF);
                        break;
                    case "point_light":
                        uboInfo.range(PointLightUBO.SIZEOF);
                        break;
                    case "simple_blur":
                        uboInfo.range(SimpleBlurInfoUBO.SIZEOF);
                        break;
                    case "spotlight":
                        uboInfo.range(SpotlightUBO.SIZEOF);
                        break;
                    case "shadow_pass_1":
                        uboInfo.range(Pass1InfoUBO.SIZEOF);
                        break;
                    case "shadow_pass_2":
                        uboInfo.range(Pass2InfoUBO.SIZEOF);
                        break;
                    case "shadow_info":
                        uboInfo.range(ShadowInfoUBO.SIZEOF);
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported uniform resource specified: " + uniformResource);
                }
            }

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(numUniformResources);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            List<Long> descriptorSets = this.getDescriptorSets();
            for (int i = 0; i < descriptorCount; i++) {
                uboDescriptorWrite.dstSet(descriptorSets.get(i));
                vkUpdateDescriptorSets(device, descriptorWrites, null);
            }
        }
    }
}
