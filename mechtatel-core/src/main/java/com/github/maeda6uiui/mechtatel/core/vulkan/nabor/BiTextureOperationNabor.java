package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.BiTextureOperationParametersUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Nabor for texture operations<br>
 * This nabor consumes two textures, applies an operation to them,
 * and returns one result texture.
 *
 * @author maeda6uiui
 */
public class BiTextureOperationNabor extends Nabor {
    public static final int COLOR_ATTACHMENT_INDEX = 0;

    public BiTextureOperationNabor(VkDevice device) {
        super(
                device,
                VK_SAMPLE_COUNT_1_BIT,
                false,
                BiTextureOperationNabor.class.getResource("/Standard/Shader/TextureOperation/texture_operation.vert"),
                BiTextureOperationNabor.class.getResource("/Standard/Shader/TextureOperation/texture_operation.frag")
        );
    }

    public void transitionColorImage(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        long colorImage = this.getImage(COLOR_ATTACHMENT_INDEX);

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
        return this.getImageView(COLOR_ATTACHMENT_INDEX);
    }

    @Override
    public void cleanup(boolean reserveForRecreation) {
        super.cleanup(reserveForRecreation);
    }

    @Override
    protected void createUniformBuffers(int descriptorCount) {
        VkDevice device = this.getDevice();

        var parametersUBOInfos = BufferUtils.createUBOBuffers(
                device, descriptorCount, BiTextureOperationParametersUBO.SIZEOF);
        for (var parametersUBOInfo : parametersUBOInfos) {
            this.getUniformBuffers().add(parametersUBOInfo.buffer);
            this.getUniformBufferMemories().add(parametersUBOInfo.bufferMemory);
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
            VkAttachmentDescription colorAttachment = attachments.get(COLOR_ATTACHMENT_INDEX);
            colorAttachment.format(colorImageFormat);
            colorAttachment.samples(msaaSamples);
            colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            colorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference colorAttachmentRef = attachmentRefs.get(COLOR_ATTACHMENT_INDEX);
            colorAttachmentRef.attachment(COLOR_ATTACHMENT_INDEX);
            colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(attachmentRefs);

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
    protected void createDescriptorSetLayouts() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            //=== set 0 ===
            VkDescriptorSetLayoutBinding.Buffer uboLayoutBindings = VkDescriptorSetLayoutBinding.calloc(1, stack);

            VkDescriptorSetLayoutBinding parametersUBOLayoutBinding = uboLayoutBindings.get(0);
            parametersUBOLayoutBinding.binding(0);
            parametersUBOLayoutBinding.descriptorCount(1);
            parametersUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            parametersUBOLayoutBinding.pImmutableSamplers(null);
            parametersUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //=== set 1 ===
            VkDescriptorSetLayoutBinding.Buffer imageLayoutBindings = VkDescriptorSetLayoutBinding.calloc(2, stack);

            VkDescriptorSetLayoutBinding colorImageLayoutBinding = imageLayoutBindings.get(0);
            colorImageLayoutBinding.binding(0);
            colorImageLayoutBinding.descriptorCount(2);
            colorImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            colorImageLayoutBinding.pImmutableSamplers(null);
            colorImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding depthImageLayoutBinding = imageLayoutBindings.get(1);
            depthImageLayoutBinding.binding(1);
            depthImageLayoutBinding.descriptorCount(2);
            depthImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            depthImageLayoutBinding.pImmutableSamplers(null);
            depthImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //=== set 2 ===
            VkDescriptorSetLayoutBinding.Buffer samplerLayoutBindings = VkDescriptorSetLayoutBinding.calloc(1, stack);

            VkDescriptorSetLayoutBinding samplerLayoutBinding = samplerLayoutBindings.get(0);
            samplerLayoutBinding.binding(0);
            samplerLayoutBinding.descriptorCount(1);
            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLER);
            samplerLayoutBinding.pImmutableSamplers(null);
            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //Create descriptor set layouts
            VkDescriptorSetLayoutCreateInfo.Buffer layoutCreateInfos = VkDescriptorSetLayoutCreateInfo.calloc(3, stack);

            //=== set 0 ===
            VkDescriptorSetLayoutCreateInfo uboLayoutCreateInfo = layoutCreateInfos.get(0);
            uboLayoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            uboLayoutCreateInfo.pBindings(uboLayoutBindings);

            //=== set 1 ===
            VkDescriptorSetLayoutCreateInfo imageLayoutCreateInfo = layoutCreateInfos.get(1);
            imageLayoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            imageLayoutCreateInfo.pBindings(imageLayoutBindings);

            //=== set 2 ===
            VkDescriptorSetLayoutCreateInfo samplerLayoutCreateInfo = layoutCreateInfos.get(2);
            samplerLayoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            samplerLayoutCreateInfo.pBindings(samplerLayoutBindings);

            for (int i = 0; i < 3; i++) {
                LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
                if (vkCreateDescriptorSetLayout(device, layoutCreateInfos.get(i), null, pDescriptorSetLayout) != VK_SUCCESS) {
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
            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.calloc(1, stack);

            VkDescriptorPoolSize parametersUBOPoolSize = uboPoolSizes.get(0);
            parametersUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            parametersUBOPoolSize.descriptorCount(descriptorCount);

            //=== set 1 ===
            VkDescriptorPoolSize.Buffer imagePoolSizes = VkDescriptorPoolSize.calloc(2, stack);

            VkDescriptorPoolSize colorImagePoolSize = imagePoolSizes.get(0);
            colorImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            colorImagePoolSize.descriptorCount(descriptorCount * 2);

            VkDescriptorPoolSize depthImagePoolSize = imagePoolSizes.get(1);
            depthImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            depthImagePoolSize.descriptorCount(descriptorCount * 2);

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
        super.createDescriptorSets(descriptorCount, commandPool, graphicsQueue);

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
            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.calloc(1, stack);

            VkDescriptorBufferInfo parametersUBOInfo = uboInfos.get(0);
            parametersUBOInfo.buffer(this.getUniformBuffer(0));
            parametersUBOInfo.offset(0);
            parametersUBOInfo.range(BiTextureOperationParametersUBO.SIZEOF);

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(1);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            //=== set 1 ===
            VkDescriptorImageInfo.Buffer imageInfos = VkDescriptorImageInfo.calloc(2 * 2, stack);
            for (int i = 0; i < 2 * 2; i++) {
                VkDescriptorImageInfo imageInfo = imageInfos.get(i);
                imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                imageInfo.imageView(this.getDummyImageView());
            }

            VkWriteDescriptorSet imageDescriptorWrite = descriptorWrites.get(1);
            imageDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            imageDescriptorWrite.dstBinding(0);
            imageDescriptorWrite.dstArrayElement(0);
            imageDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            imageDescriptorWrite.descriptorCount(2 * 2);
            imageDescriptorWrite.pImageInfo(imageInfos);

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
            }

            for (int i = 0; i < descriptorSets.size(); i++) {
                this.getDescriptorSets().add(descriptorSets.get(i));
            }
        }
    }

    @Override
    protected void createGraphicsPipelines() {
        VkDevice device = this.getDevice();
        int msaaSamples = this.getMsaaSamples();
        VkExtent2D extent = this.getExtent();

        this.setupShaderModules(true);
        long vertShaderModule = this.getVertShaderModule(0);
        long fragShaderModule = this.getFragShaderModule(0);

        try (MemoryStack stack = MemoryStack.stackPush()) {
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
            depthStencil.depthTestEnable(false);
            depthStencil.depthWriteEnable(false);
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

    public void bindColorImages(VkCommandBuffer commandBuffer, List<Long> imageViews) {
        this.bindImages(commandBuffer, 1, 0, imageViews);
    }

    public void bindDepthImages(VkCommandBuffer commandBuffer, List<Long> imageViews) {
        this.bindImages(commandBuffer, 1, 1, imageViews);
    }

    public void copyColorImage(long commandPool, VkQueue graphicsQueue, long colorDstImage) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();
            VkExtent2D extent = this.getExtent();

            VkImageCopy.Buffer imageCopyRegion = VkImageCopy.calloc(1, stack);
            imageCopyRegion.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            imageCopyRegion.srcSubresource().layerCount(1);
            imageCopyRegion.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            imageCopyRegion.dstSubresource().layerCount(1);
            imageCopyRegion.extent(VkExtent3D.calloc(stack).set(extent.width(), extent.height(), 1));

            long colorSrcImage = this.getImage(0);

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            ImageUtils.transitionImageLayout(
                    commandBuffer,
                    colorSrcImage,
                    VK_IMAGE_ASPECT_COLOR_BIT,
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    1);
            ImageUtils.transitionImageLayout(
                    commandBuffer,
                    colorDstImage,
                    VK_IMAGE_ASPECT_COLOR_BIT,
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    1
            );

            vkCmdCopyImage(
                    commandBuffer,
                    colorSrcImage,
                    VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                    colorDstImage,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    imageCopyRegion);

            ImageUtils.transitionImageLayout(
                    commandBuffer,
                    colorDstImage,
                    VK_IMAGE_ASPECT_COLOR_BIT,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    1);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }
}
