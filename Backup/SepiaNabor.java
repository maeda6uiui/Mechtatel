package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkVertex3DUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.DepthResourceUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ShaderSPIRVUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.vulkan.KHRSwapchain.VK_IMAGE_LAYOUT_PRESENT_SRC_KHR;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Nabor for rendering with "simple" shader followed by "sepia" post processing
 */
public class SepiaNabor extends Nabor {
    public SepiaNabor(VkDevice device, int imageFormat, int msaaSamples, int width, int height) {
        super(device, imageFormat, msaaSamples, width, height);
    }

    private static class RenderPassInfo {
        public VkAttachmentDescription.Buffer attachments;
        public VkAttachmentReference.Buffer attachmentRefs;
        public VkSubpassDescription subpass;
        public VkSubpassDependency dependency;
    }

    private RenderPassInfo createFirstRenderPassInfo(int imageFormat, int msaaSamples) {
        MemoryStack stack = MemoryStack.stackGet();

        VkDevice device = this.getDevice();

        var renderPassInfo = new RenderPassInfo();
        renderPassInfo.attachments = VkAttachmentDescription.calloc(2, stack);
        renderPassInfo.attachmentRefs = VkAttachmentReference.calloc(2, stack);
        renderPassInfo.subpass = VkSubpassDescription.calloc(stack);
        renderPassInfo.dependency = VkSubpassDependency.calloc(stack);

        //Color attachments
        VkAttachmentDescription colorAttachment = renderPassInfo.attachments.get(0);
        colorAttachment.format(imageFormat);
        colorAttachment.samples(msaaSamples);
        colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
        colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
        colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        colorAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        colorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkAttachmentReference colorAttachmentRef = renderPassInfo.attachmentRefs.get(0);
        colorAttachmentRef.attachment(0);
        colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        //Depth-stencil attachments
        VkAttachmentDescription depthAttachment = renderPassInfo.attachments.get(1);
        depthAttachment.format(DepthResourceUtils.findDepthFormat(device));
        depthAttachment.samples(msaaSamples);
        depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
        depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        VkAttachmentReference depthAttachmentRef = renderPassInfo.attachmentRefs.get(1);
        depthAttachmentRef.attachment(1);
        depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

        //Subpass
        renderPassInfo.subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
        renderPassInfo.subpass.colorAttachmentCount(1);
        renderPassInfo.subpass.pColorAttachments(VkAttachmentReference.calloc(1, stack).put(0, colorAttachmentRef));
        renderPassInfo.subpass.pDepthStencilAttachment(depthAttachmentRef);

        //Dependency
        renderPassInfo.dependency.srcSubpass(0);
        renderPassInfo.dependency.dstSubpass(1);
        renderPassInfo.dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        renderPassInfo.dependency.srcAccessMask(0);
        renderPassInfo.dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        renderPassInfo.dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        return renderPassInfo;
    }

    private RenderPassInfo createSecondRenderPassInfo(int imageFormat) {
        MemoryStack stack = MemoryStack.stackGet();

        VkDevice device = this.getDevice();

        var renderPassInfo = new RenderPassInfo();
        renderPassInfo.attachments = VkAttachmentDescription.calloc(2, stack);
        renderPassInfo.attachmentRefs = VkAttachmentReference.calloc(2, stack);

        //Color attachments
        VkAttachmentDescription colorAttachment = renderPassInfo.attachments.get(0);
        colorAttachment.format(imageFormat);
        colorAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
        colorAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_LOAD);
        colorAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
        colorAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        colorAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        colorAttachment.initialLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);
        colorAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        VkAttachmentReference colorAttachmentRef = renderPassInfo.attachmentRefs.get(0);
        colorAttachmentRef.attachment(0);
        colorAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        //Present image
        VkAttachmentDescription colorAttachmentResolve = renderPassInfo.attachments.get(1);
        colorAttachmentResolve.format(imageFormat);
        colorAttachmentResolve.samples(VK_SAMPLE_COUNT_1_BIT);
        colorAttachmentResolve.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        colorAttachmentResolve.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
        colorAttachmentResolve.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
        colorAttachmentResolve.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
        colorAttachmentResolve.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
        colorAttachmentResolve.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

        VkAttachmentReference colorAttachmentResolveRef = renderPassInfo.attachmentRefs.get(1);
        colorAttachmentResolveRef.attachment(1);
        colorAttachmentResolveRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

        //Subpass
        renderPassInfo.subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
        renderPassInfo.subpass.colorAttachmentCount(1);
        renderPassInfo.subpass.pColorAttachments(VkAttachmentReference.calloc(1, stack).put(0, colorAttachmentRef));
        renderPassInfo.subpass.pResolveAttachments(VkAttachmentReference.calloc(1, stack).put(0, colorAttachmentResolveRef));

        //Dependency
        renderPassInfo.dependency.srcSubpass(1);
        renderPassInfo.dependency.dstSubpass(VK_SUBPASS_EXTERNAL);
        renderPassInfo.dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        renderPassInfo.dependency.srcAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);
        renderPassInfo.dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
        renderPassInfo.dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

        return renderPassInfo;
    }

    @Override
    protected void createRenderPass(int imageFormat, int msaaSamples) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            RenderPassInfo firstRenderPassInfo = this.createFirstRenderPassInfo(imageFormat, msaaSamples);
            RenderPassInfo secondRenderPassInfo = this.createSecondRenderPassInfo(imageFormat);

            VkAttachmentDescription.Buffer attachments
                    = VkAttachmentDescription.calloc(4, stack)
                    .put(firstRenderPassInfo.attachments)
                    .put(secondRenderPassInfo.attachments);
            VkSubpassDescription.Buffer subpasses
                    = VkSubpassDescription.calloc(2, stack)
                    .put(0, firstRenderPassInfo.subpass).put(1, secondRenderPassInfo.subpass);
            VkSubpassDependency.Buffer dependencies
                    = VkSubpassDependency.calloc(2, stack)
                    .put(0, firstRenderPassInfo.dependency).put(1, secondRenderPassInfo.dependency);

            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.calloc(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_CREATE_INFO);
            renderPassInfo.pAttachments(attachments);
            renderPassInfo.pSubpasses(subpasses);
            renderPassInfo.pDependencies(dependencies);

            LongBuffer pRenderPass = stack.mallocLong(1);
            if (vkCreateRenderPass(device, renderPassInfo, null, pRenderPass) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a render pass");
            }

            this.setRenderPass(pRenderPass.get(0));
        }
    }

    @Override
    protected void createDescriptorSetLayout() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            VkDescriptorSetLayoutBinding.Buffer bindings = VkDescriptorSetLayoutBinding.calloc(2, stack);

            VkDescriptorSetLayoutBinding cameraUBOLayoutBinding = bindings.get(0);
            cameraUBOLayoutBinding.binding(0);
            cameraUBOLayoutBinding.descriptorCount(1);
            cameraUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBOLayoutBinding.pImmutableSamplers(null);
            cameraUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

            VkDescriptorSetLayoutBinding samplerLayoutBinding = bindings.get(1);
            samplerLayoutBinding.binding(1);
            samplerLayoutBinding.descriptorCount(1);
            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_COMBINED_IMAGE_SAMPLER);
            samplerLayoutBinding.pImmutableSamplers(null);
            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutCreateInfo layoutInfo = VkDescriptorSetLayoutCreateInfo.calloc(stack);
            layoutInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            layoutInfo.pBindings(bindings);

            LongBuffer pDescriptorSetLayout = stack.mallocLong(1);
            if (vkCreateDescriptorSetLayout(device, layoutInfo, null, pDescriptorSetLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a descriptor set layout");
            }

            long descriptorSetLayout = pDescriptorSetLayout.get(0);
            this.getDescriptorSetLayouts().add(descriptorSetLayout);
        }
    }

    @Override
    protected void createGraphicsPipeline(int width, int height, int msaaSamples) {
        VkDevice device = this.getDevice();

        long vertShaderModule;
        long fragShaderModule;
        if (this.getVertShaderModules().size() != 0) {
            vertShaderModule = this.getVertShaderModules().get(0);
            fragShaderModule = this.getFragShaderModules().get(0);
        } else {
            final String vertShaderFilepath = "./Mechtatel/Shader/Standard/3d_simple.vert";
            final String fragShaderFilepath = "./Mechtatel/Shader/Standard/3d_simple.frag";

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
            vertexInputInfo.pVertexBindingDescriptions(VkVertex3DUV.getBindingDescription());
            vertexInputInfo.pVertexAttributeDescriptions(VkVertex3DUV.getAttributeDescriptions());

            //Assembly stage
            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.calloc(stack);
            inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            inputAssembly.primitiveRestartEnable(false);

            //Viewport and scissor
            VkViewport.Buffer viewport = VkViewport.calloc(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(width);
            viewport.height(height);
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.calloc(1, stack);
            scissor.offset(VkOffset2D.calloc(stack).set(0, 0));
            scissor.extent(VkExtent2D.calloc(stack).set(width, height));

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
            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachment = VkPipelineColorBlendAttachmentState.calloc(1, stack);
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
            colorBlending.pAttachments(colorBlendAttachment);
            colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

            //Push constant
            //Push a model matrix (mat4)
            VkPushConstantRange.Buffer pushConstant = VkPushConstantRange.calloc(1, stack);
            pushConstant.offset(0);
            pushConstant.size(1 * 16 * Float.BYTES);
            pushConstant.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

            //Pipeline layout creation
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(stack.longs(this.getDescriptorSetLayout(0)));
            pipelineLayoutInfo.pPushConstantRanges(pushConstant);

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
}
