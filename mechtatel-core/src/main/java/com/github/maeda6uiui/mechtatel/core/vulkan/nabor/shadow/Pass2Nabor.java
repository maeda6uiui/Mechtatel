package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.shadow;

import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkMttVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.Nabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.Pass2InfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow.ShadowInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Nabor to apply shadow mapping with images obtained from pass 1
 *
 * @author maeda6uiui
 */
class Pass2Nabor extends Nabor {
    public static final int MAX_NUM_SHADOW_MAPS = 16;

    public Pass2Nabor(VkDevice device) {
        super(
                device,
                VK_SAMPLE_COUNT_1_BIT,
                false,
                Pass2Nabor.class.getResource("/Standard/Shader/ShadowMapping/pass_2.vert"),
                Pass2Nabor.class.getResource("/Standard/Shader/ShadowMapping/pass_2.frag")
        );
    }

    public void transitionColorImageLayout(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        long colorImage = this.getImage(0);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                colorImage,
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1);
    }

    public long getColorImageView() {
        return this.getImageView(0);
    }

    @Override
    protected void createTextureSamplers(int filter, int mipmapMode, int addressMode) {
        VkDevice device = this.getDevice();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.calloc(stack);
            samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerInfo.magFilter(filter);
            samplerInfo.minFilter(filter);
            samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER);
            samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER);
            samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_CLAMP_TO_BORDER);
            samplerInfo.anisotropyEnable(true);
            samplerInfo.maxAnisotropy(16.0f);
            samplerInfo.borderColor(VK_BORDER_COLOR_FLOAT_OPAQUE_WHITE);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerInfo.mipmapMode(mipmapMode);
            samplerInfo.minLod(0.0f);
            samplerInfo.maxLod(10.0f);
            samplerInfo.mipLodBias(0.0f);

            LongBuffer pTextureSampler = stack.mallocLong(1);
            if (vkCreateSampler(device, samplerInfo, null, pTextureSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a texture sampler");
            }

            long textureSampler = pTextureSampler.get(0);
            this.getTextureSamplers().add(textureSampler);
        }
    }

    @Override
    protected void createUniformBuffers(int descriptorCount) {
        VkDevice device = this.getDevice();

        var pass2InfoUBOInfos = BufferCreator.createUBOBuffers(
                device, descriptorCount, Pass2InfoUBO.SIZEOF);
        for (var pass2InfoUBOInfo : pass2InfoUBOInfos) {
            this.getUniformBuffers().add(pass2InfoUBOInfo.buffer);
            this.getUniformBufferMemories().add(pass2InfoUBOInfo.bufferMemory);
        }

        var shadowInfoUBOInfos = BufferCreator.createUBOBuffers(
                device, descriptorCount, ShadowInfoUBO.SIZEOF * MAX_NUM_SHADOW_MAPS);
        for (var shadowInfoUBOInfo : shadowInfoUBOInfos) {
            this.getUniformBuffers().add(shadowInfoUBOInfo.buffer);
            this.getUniformBufferMemories().add(shadowInfoUBOInfo.bufferMemory);
        }
    }

    @Override
    protected void createDescriptorSetLayouts() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            //=== set 0 ===
            VkDescriptorSetLayoutBinding.Buffer uboBindings = VkDescriptorSetLayoutBinding.calloc(2, stack);

            VkDescriptorSetLayoutBinding pass2InfoUBOLayoutBinding = uboBindings.get(0);
            pass2InfoUBOLayoutBinding.binding(0);
            pass2InfoUBOLayoutBinding.descriptorCount(1);
            pass2InfoUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            pass2InfoUBOLayoutBinding.pImmutableSamplers(null);
            pass2InfoUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding shadowInfoUBOLayoutBinding = uboBindings.get(1);
            shadowInfoUBOLayoutBinding.binding(1);
            shadowInfoUBOLayoutBinding.descriptorCount(1);
            shadowInfoUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            shadowInfoUBOLayoutBinding.pImmutableSamplers(null);
            shadowInfoUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //=== set 1 ===
            VkDescriptorSetLayoutBinding.Buffer imageBindings = VkDescriptorSetLayoutBinding.calloc(5, stack);

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

            VkDescriptorSetLayoutBinding shadowDepthImageLayoutBinding = imageBindings.get(4);
            shadowDepthImageLayoutBinding.binding(4);
            shadowDepthImageLayoutBinding.descriptorCount(MAX_NUM_SHADOW_MAPS);
            shadowDepthImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            shadowDepthImageLayoutBinding.pImmutableSamplers(null);
            shadowDepthImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //=== set 2 ===
            VkDescriptorSetLayoutBinding.Buffer samplerBindings = VkDescriptorSetLayoutBinding.calloc(1, stack);

            VkDescriptorSetLayoutBinding samplerLayoutBinding = samplerBindings.get(0);
            samplerLayoutBinding.binding(0);
            samplerLayoutBinding.descriptorCount(1);
            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLER);
            samplerLayoutBinding.pImmutableSamplers(null);
            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //Create descriptor set layouts
            VkDescriptorSetLayoutCreateInfo.Buffer layoutInfos = VkDescriptorSetLayoutCreateInfo.calloc(3, stack);

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
            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.calloc(2, stack);

            VkDescriptorPoolSize pass2InfoUBOPoolSize = uboPoolSizes.get(0);
            pass2InfoUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            pass2InfoUBOPoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize shadowInfoUBOPoolSize = uboPoolSizes.get(1);
            shadowInfoUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            shadowInfoUBOPoolSize.descriptorCount(descriptorCount * MAX_NUM_SHADOW_MAPS);

            //=== set 1 ===
            VkDescriptorPoolSize.Buffer imagePoolSizes = VkDescriptorPoolSize.calloc(5, stack);

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

            VkDescriptorPoolSize shadowDepthImagePoolSize = imagePoolSizes.get(4);
            shadowDepthImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            shadowDepthImagePoolSize.descriptorCount(descriptorCount * MAX_NUM_SHADOW_MAPS);

            //=== set 2 ===
            VkDescriptorPoolSize.Buffer samplerPoolSizes = VkDescriptorPoolSize.calloc(1, stack);

            VkDescriptorPoolSize samplerPoolSize = samplerPoolSizes.get(0);
            samplerPoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLER);
            samplerPoolSize.descriptorCount(descriptorCount);

            //Create descriptor pools
            VkDescriptorPoolCreateInfo.Buffer poolInfos = VkDescriptorPoolCreateInfo.calloc(3, stack);

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
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            List<Long> descriptorSetLayouts = this.getDescriptorSetLayouts();
            List<Long> descriptorPools = this.getDescriptorPools();
            List<Long> descriptorSets = new ArrayList<>();

            int setCount = descriptorSetLayouts.size();
            this.setSetCount(setCount);

            VkDescriptorSetAllocateInfo.Buffer allocInfos = VkDescriptorSetAllocateInfo.calloc(setCount, stack);

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

            VkWriteDescriptorSet.Buffer descriptorWrites = VkWriteDescriptorSet.calloc(setCount, stack);

            //=== set 0 ===
            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.calloc(2, stack);

            VkDescriptorBufferInfo pass2InfoUBOInfo = uboInfos.get(0);
            pass2InfoUBOInfo.buffer(this.getUniformBuffer(0));
            pass2InfoUBOInfo.offset(0);
            pass2InfoUBOInfo.range(Pass2InfoUBO.SIZEOF);

            VkDescriptorBufferInfo shadowInfoUBOInfo = uboInfos.get(1);
            shadowInfoUBOInfo.buffer(this.getUniformBuffer(1));
            shadowInfoUBOInfo.offset(0);
            shadowInfoUBOInfo.range(ShadowInfoUBO.SIZEOF * MAX_NUM_SHADOW_MAPS);

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(2);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            //=== set 1 ===
            VkDescriptorImageInfo.Buffer imageInfos = VkDescriptorImageInfo.calloc(4, stack);
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

            VkWriteDescriptorSet.Buffer shadowDepthImageDescriptorWrite = VkWriteDescriptorSet.calloc(1, stack);
            VkDescriptorImageInfo.Buffer shadowDepthImageInfos = VkDescriptorImageInfo.calloc(MAX_NUM_SHADOW_MAPS, stack);
            for (int i = 0; i < MAX_NUM_SHADOW_MAPS; i++) {
                VkDescriptorImageInfo imageInfo = shadowDepthImageInfos.get(i);
                imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                imageInfo.imageView(this.getDummyImageView());
            }

            shadowDepthImageDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            shadowDepthImageDescriptorWrite.dstBinding(4);
            shadowDepthImageDescriptorWrite.dstArrayElement(0);
            shadowDepthImageDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            shadowDepthImageDescriptorWrite.descriptorCount(MAX_NUM_SHADOW_MAPS);
            shadowDepthImageDescriptorWrite.pImageInfo(shadowDepthImageInfos);

            //=== set 2 ===
            VkDescriptorImageInfo.Buffer samplerInfos = VkDescriptorImageInfo.calloc(1, stack);

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

                shadowDepthImageDescriptorWrite.dstSet(descriptorSets.get(i + descriptorCount));
                vkUpdateDescriptorSets(device, shadowDepthImageDescriptorWrite, null);
            }

            for (int i = 0; i < descriptorSets.size(); i++) {
                this.getDescriptorSets().add(descriptorSets.get(i));
            }
        }
    }

    @Override
    protected void createRenderPass(int colorImageFormat) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();
            int msaaSamples = this.getMsaaSamples();

            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(1, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.calloc(1, stack);

            //Color attachment
            VkAttachmentDescription colorAttachment = attachments.get(0);
            colorAttachment.format(colorImageFormat);
            colorAttachment.samples(msaaSamples);
            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            colorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference colorAttachmentRef = attachmentRefs.get(0);
            colorAttachmentRef.attachment(0);
            colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference.Buffer colorAttachmentRefs = VkAttachmentReference.calloc(1, stack);
            colorAttachmentRefs.put(0, colorAttachmentRef);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(colorAttachmentRefs);

            VkSubpassDependency.Buffer dependency = VkSubpassDependency.calloc(1, stack);
            dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency.dstSubpass(0);
            dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.srcAccessMask(0);
            dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassInfo.pAttachments(attachments);
            renderPassInfo.pSubpasses(subpass);
            renderPassInfo.pDependencies(dependency);

            LongBuffer pRenderPass = stack.mallocLong(1);
            if (vkCreateRenderPass(device, renderPassInfo, null, pRenderPass) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a render pass");
            }

            long renderPass = pRenderPass.get(0);
            this.setRenderPass(renderPass);
        }
    }

    @Override
    protected void createGraphicsPipelines() {
        if (this.isContainer()) {
            return;
        }

        VkDevice device = this.getDevice();

        this.setupShaderModules(true);
        long vertShaderModule = this.getVertShaderModule(0);
        long fragShaderModule = this.getFragShaderModule(0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkExtent2D extent = this.getExtent();
            int msaaSamples = this.getMsaaSamples();

            ByteBuffer entryPoint = stack.UTF8("main");

            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.calloc(2, stack);

            VkPipelineShaderStageCreateInfo vertShaderStageInfo = shaderStages.get(0);
            vertShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            vertShaderStageInfo.stage(VK_SHADER_STAGE_VERTEX_BIT);
            vertShaderStageInfo.module(vertShaderModule);
            vertShaderStageInfo.pName(entryPoint);

            VkPipelineShaderStageCreateInfo fragShaderStageInfo = shaderStages.get(1);
            fragShaderStageInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_SHADER_STAGE_CREATE_INFO);
            fragShaderStageInfo.stage(VK_SHADER_STAGE_FRAGMENT_BIT);
            fragShaderStageInfo.module(fragShaderModule);
            fragShaderStageInfo.pName(entryPoint);

            //Vertex stage
            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.calloc(stack);
            vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
            vertexInputInfo.pVertexBindingDescriptions(VkMttVertex2DUV.getBindingDescription());
            vertexInputInfo.pVertexAttributeDescriptions(VkMttVertex2DUV.getAttributeDescriptions());

            //Assembly stage
            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
            inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            inputAssembly.primitiveRestartEnable(false);

            //Viewport and scissor
            VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(extent.width());
            viewport.height(extent.height());
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
            scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
            scissor.extent(extent);

            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.calloc(stack);
            viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
            viewportState.pViewports(viewport);
            viewportState.pScissors(scissor);

            //Rasterization stage
            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.calloc(stack);
            rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
            rasterizer.depthClampEnable(false);
            rasterizer.rasterizerDiscardEnable(false);
            rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
            rasterizer.lineWidth(1.0f);
            rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
            rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
            rasterizer.depthBiasEnable(false);

            //Multisampling
            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.calloc(stack);
            multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
            multisampling.sampleShadingEnable(true);
            multisampling.minSampleShading(0.2f);
            multisampling.rasterizationSamples(msaaSamples);

            //Depth-stencil
            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.calloc(stack);
            depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
            depthStencil.depthTestEnable(true);
            depthStencil.depthWriteEnable(true);
            depthStencil.depthCompareOp(VK_COMPARE_OP_LESS);
            depthStencil.depthBoundsTestEnable(false);
            depthStencil.minDepthBounds(0.0f);
            depthStencil.maxDepthBounds(1.0f);
            depthStencil.stencilTestEnable(false);

            //Color blending
            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachments = VkPipelineColorBlendAttachmentState.calloc(1, stack);

            VkPipelineColorBlendAttachmentState colorBlendAttachment = colorBlendAttachments.get(0);
            colorBlendAttachment.colorWriteMask(
                    VK_COLOR_COMPONENT_R_BIT |
                            VK_COLOR_COMPONENT_G_BIT |
                            VK_COLOR_COMPONENT_B_BIT |
                            VK_COLOR_COMPONENT_A_BIT);
            colorBlendAttachment.blendEnable(false);

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
            colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
            colorBlending.logicOpEnable(false);
            colorBlending.logicOp(VK_LOGIC_OP_COPY);
            colorBlending.pAttachments(colorBlendAttachments);
            colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

            //Pipeline layout creation
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(this.pDescriptorSetLayouts());

            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            if (vkCreatePipelineLayout(device, pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a pipeline layout");
            }

            long pipelineLayout = pPipelineLayout.get(0);
            this.getPipelineLayouts().add(pipelineLayout);

            //Graphics pipeline creation
            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.calloc(1, stack);
            pipelineInfo.sType(VK_STRUCTURE_TYPE_GRAPHICS_PIPELINE_CREATE_INFO);
            pipelineInfo.pStages(shaderStages);
            pipelineInfo.pVertexInputState(vertexInputInfo);
            pipelineInfo.pInputAssemblyState(inputAssembly);
            pipelineInfo.pViewportState(viewportState);
            pipelineInfo.pRasterizationState(rasterizer);
            pipelineInfo.pMultisampleState(multisampling);
            pipelineInfo.pDepthStencilState(depthStencil);
            pipelineInfo.pColorBlendState(colorBlending);
            pipelineInfo.layout(pipelineLayout);
            pipelineInfo.renderPass(this.getRenderPass());
            pipelineInfo.subpass(0);
            pipelineInfo.basePipelineHandle(VK_NULL_HANDLE);
            pipelineInfo.basePipelineIndex(-1);

            LongBuffer pGraphicsPipeline = stack.mallocLong(1);
            if (vkCreateGraphicsPipelines(device, VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a graphics pipeline");
            }

            long graphicsPipeline = pGraphicsPipeline.get(0);
            this.getGraphicsPipelines().add(graphicsPipeline);
        }
    }

    @Override
    protected void createImages(int colorImageFormat) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();
            int msaaSamples = this.getMsaaSamples();
            VkExtent2D extent = this.getExtent();

            LongBuffer pImage = stack.mallocLong(1);
            LongBuffer pImageMemory = stack.mallocLong(1);
            LongBuffer pImageView = stack.mallocLong(1);

            //Color image
            ImageUtils.createImage(
                    device,
                    extent.width(),
                    extent.height(),
                    1,
                    msaaSamples,
                    colorImageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT | VK_IMAGE_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            long colorImage = pImage.get(0);
            long colorImageMemory = pImageMemory.get(0);

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.image(colorImage);
            viewInfo.format(colorImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(1);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            long colorImageView = pImageView.get(0);

            this.getImages().add(colorImage);
            this.getImageMemories().add(colorImageMemory);
            this.getImageViews().add(colorImageView);
        }
    }

    public void bindImages(
            VkCommandBuffer commandBuffer,
            int dstBinding,
            long albedoImageView,
            long depthImageView,
            long positionImageView,
            long normalImageView) {
        var arrImageViews = new Long[]{albedoImageView, depthImageView, positionImageView, normalImageView};
        var imageViews = Arrays.asList(arrImageViews);

        this.bindImages(commandBuffer, 1, dstBinding, imageViews);
    }
}
