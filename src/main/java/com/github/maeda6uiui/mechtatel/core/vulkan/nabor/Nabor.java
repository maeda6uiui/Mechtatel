package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import com.github.maeda6uiui.mechtatel.core.vulkan.component.VkVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Set of necessary objects for rendering with Vulkan
 *
 * @author maeda
 */
public class Nabor {
    private VkDevice device;

    private int msaaSamples;
    private VkExtent2D extent;
    private boolean isContainer;

    private long dummyImage;
    private long dummyImageMemory;
    private long dummyImageView;

    private List<Long> textureSamplers;

    private List<Long> uniformBuffers;
    private List<Long> uniformBufferMemories;

    private long renderPass;
    private List<Long> descriptorSetLayouts;
    private List<Long> descriptorPools;
    private List<Long> descriptorSets;
    private List<Long> vertShaderModules;
    private List<Long> fragShaderModules;
    private List<Long> pipelineLayouts;
    private List<Long> graphicsPipelines;

    private List<Long> images;
    private List<Long> imageMemories;
    private List<Long> imageViews;
    private List<Long> framebuffers;

    private int setCount;

    public Nabor(VkDevice device, int msaaSamples, boolean isContainer) {
        this.device = device;

        this.msaaSamples = msaaSamples;
        this.isContainer = isContainer;

        textureSamplers = new ArrayList<>();

        uniformBuffers = new ArrayList<>();
        uniformBufferMemories = new ArrayList<>();

        descriptorSetLayouts = new ArrayList<>();
        descriptorPools = new ArrayList<>();
        descriptorSets = new ArrayList<>();
        vertShaderModules = new ArrayList<>();
        fragShaderModules = new ArrayList<>();
        pipelineLayouts = new ArrayList<>();
        graphicsPipelines = new ArrayList<>();

        images = new ArrayList<>();
        imageMemories = new ArrayList<>();
        imageViews = new ArrayList<>();
        framebuffers = new ArrayList<>();
    }

    protected long getDummyImageView() {
        return dummyImageView;
    }

    public void transitionImage(long commandPool, VkQueue graphicsQueue, int index, int oldLayout) {
        VkDevice device = this.getDevice();
        long image = this.getImage(index);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                image,
                false,
                oldLayout,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1);
    }

    protected void createTextureSamplers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSamplerCreateInfo samplerInfo = VkSamplerCreateInfo.callocStack(stack);
            samplerInfo.sType(VK_STRUCTURE_TYPE_SAMPLER_CREATE_INFO);
            samplerInfo.magFilter(VK_FILTER_LINEAR);
            samplerInfo.minFilter(VK_FILTER_LINEAR);
            samplerInfo.addressModeU(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeV(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.addressModeW(VK_SAMPLER_ADDRESS_MODE_REPEAT);
            samplerInfo.anisotropyEnable(true);
            samplerInfo.maxAnisotropy(16.0f);
            samplerInfo.borderColor(VK_BORDER_COLOR_INT_OPAQUE_BLACK);
            samplerInfo.unnormalizedCoordinates(false);
            samplerInfo.compareEnable(false);
            samplerInfo.compareOp(VK_COMPARE_OP_ALWAYS);
            samplerInfo.mipmapMode(VK_SAMPLER_MIPMAP_MODE_LINEAR);
            samplerInfo.minLod(0.0f);
            samplerInfo.maxLod(10.0f);
            samplerInfo.mipLodBias(0.0f);

            LongBuffer pTextureSampler = stack.mallocLong(1);
            if (vkCreateSampler(device, samplerInfo, null, pTextureSampler) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a texture sampler");
            }

            long textureSampler = pTextureSampler.get(0);
            textureSamplers.add(textureSampler);
        }
    }

    protected void createUniformBuffers(int descriptorCount) {

    }

    protected void createRenderPass(int colorImageFormat) {

    }

    protected void createDescriptorSetLayouts() {

    }

    protected void createDescriptorPools(int descriptorCount) {

    }

    private void createDummyImage(long commandPool, VkQueue graphicsQueue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            LongBuffer pImage = stack.mallocLong(1);
            LongBuffer pImageMemory = stack.mallocLong(1);
            LongBuffer pImageView = stack.mallocLong(1);

            //Color image
            ImageUtils.createImage(
                    device,
                    1,
                    1,
                    1,
                    1,
                    VK_FORMAT_R8_UNORM,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            dummyImage = pImage.get(0);
            dummyImageMemory = pImageMemory.get(0);

            ImageUtils.transitionImageLayout(
                    device,
                    commandPool,
                    graphicsQueue,
                    dummyImage,
                    false,
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    1);

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.image(dummyImage);
            viewInfo.format(VK_FORMAT_R8_UNORM);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(1);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            dummyImageView = pImageView.get(0);
        }
    }

    protected void createDescriptorSets(int descriptorCount, long commandPool, VkQueue graphicsQueue) {
        this.createDummyImage(commandPool, graphicsQueue);
    }

    protected void createGraphicsPipelines(long vertShaderModule, long fragShaderModule) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();
            VkExtent2D extent = this.getExtent();
            int msaaSamples = this.getMsaaSamples();

            ByteBuffer entryPoint = stack.UTF8("main");

            VkPipelineShaderStageCreateInfo.Buffer shaderStages = VkPipelineShaderStageCreateInfo.callocStack(2, stack);

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
            VkPipelineVertexInputStateCreateInfo vertexInputInfo = VkPipelineVertexInputStateCreateInfo.callocStack(stack);
            vertexInputInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_VERTEX_INPUT_STATE_CREATE_INFO);
            vertexInputInfo.pVertexBindingDescriptions(VkVertex2DUV.getBindingDescription());
            vertexInputInfo.pVertexAttributeDescriptions(VkVertex2DUV.getAttributeDescriptions());

            //Assembly stage
            VkPipelineInputAssemblyStateCreateInfo inputAssembly = VkPipelineInputAssemblyStateCreateInfo.callocStack(stack);
            inputAssembly.sType(VK_STRUCTURE_TYPE_PIPELINE_INPUT_ASSEMBLY_STATE_CREATE_INFO);
            inputAssembly.topology(VK_PRIMITIVE_TOPOLOGY_TRIANGLE_LIST);
            inputAssembly.primitiveRestartEnable(false);

            //Viewport and scissor
            VkViewport.Buffer viewport = VkViewport.callocStack(1, stack);
            viewport.x(0.0f);
            viewport.y(0.0f);
            viewport.width(extent.width());
            viewport.height(extent.height());
            viewport.minDepth(0.0f);
            viewport.maxDepth(1.0f);

            VkRect2D.Buffer scissor = VkRect2D.callocStack(1, stack);
            scissor.offset(VkOffset2D.callocStack(stack).set(0, 0));
            scissor.extent(extent);

            VkPipelineViewportStateCreateInfo viewportState = VkPipelineViewportStateCreateInfo.callocStack(stack);
            viewportState.sType(VK_STRUCTURE_TYPE_PIPELINE_VIEWPORT_STATE_CREATE_INFO);
            viewportState.pViewports(viewport);
            viewportState.pScissors(scissor);

            //Rasterization stage
            VkPipelineRasterizationStateCreateInfo rasterizer = VkPipelineRasterizationStateCreateInfo.callocStack(stack);
            rasterizer.sType(VK_STRUCTURE_TYPE_PIPELINE_RASTERIZATION_STATE_CREATE_INFO);
            rasterizer.depthClampEnable(false);
            rasterizer.rasterizerDiscardEnable(false);
            rasterizer.polygonMode(VK_POLYGON_MODE_FILL);
            rasterizer.lineWidth(1.0f);
            rasterizer.cullMode(VK_CULL_MODE_BACK_BIT);
            rasterizer.frontFace(VK_FRONT_FACE_COUNTER_CLOCKWISE);
            rasterizer.depthBiasEnable(false);

            //Multisampling
            VkPipelineMultisampleStateCreateInfo multisampling = VkPipelineMultisampleStateCreateInfo.callocStack(stack);
            multisampling.sType(VK_STRUCTURE_TYPE_PIPELINE_MULTISAMPLE_STATE_CREATE_INFO);
            multisampling.sampleShadingEnable(true);
            multisampling.minSampleShading(0.2f);
            multisampling.rasterizationSamples(msaaSamples);

            //Depth-stencil
            VkPipelineDepthStencilStateCreateInfo depthStencil = VkPipelineDepthStencilStateCreateInfo.callocStack(stack);
            depthStencil.sType(VK_STRUCTURE_TYPE_PIPELINE_DEPTH_STENCIL_STATE_CREATE_INFO);
            depthStencil.depthTestEnable(true);
            depthStencil.depthWriteEnable(true);
            depthStencil.depthCompareOp(VK_COMPARE_OP_LESS);
            depthStencil.depthBoundsTestEnable(false);
            depthStencil.minDepthBounds(0.0f);
            depthStencil.maxDepthBounds(1.0f);
            depthStencil.stencilTestEnable(false);

            //Color blending
            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachments = VkPipelineColorBlendAttachmentState.callocStack(1, stack);

            VkPipelineColorBlendAttachmentState colorBlendAttachment = colorBlendAttachments.get(0);
            colorBlendAttachment.colorWriteMask(
                    VK_COLOR_COMPONENT_R_BIT |
                            VK_COLOR_COMPONENT_G_BIT |
                            VK_COLOR_COMPONENT_B_BIT |
                            VK_COLOR_COMPONENT_A_BIT);
            colorBlendAttachment.blendEnable(false);

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.callocStack(stack);
            colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
            colorBlending.logicOpEnable(false);
            colorBlending.logicOp(VK_LOGIC_OP_COPY);
            colorBlending.pAttachments(colorBlendAttachments);
            colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

            //Pipeline layout creation
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.callocStack(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(this.pDescriptorSetLayouts());

            LongBuffer pPipelineLayout = stack.longs(VK_NULL_HANDLE);
            if (vkCreatePipelineLayout(device, pipelineLayoutInfo, null, pPipelineLayout) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a pipeline layout");
            }

            long pipelineLayout = pPipelineLayout.get(0);
            this.getPipelineLayouts().add(pipelineLayout);

            //Graphics pipeline creation
            VkGraphicsPipelineCreateInfo.Buffer pipelineInfo = VkGraphicsPipelineCreateInfo.callocStack(1, stack);
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

    protected void createGraphicsPipelines() {

    }

    protected void createImages(
            long commandPool,
            VkQueue graphicsQueue,
            int colorImageFormat) {

    }

    protected void createFramebuffers() {
        if (imageViews.size() == 0) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();
            VkExtent2D extent = this.getExtent();

            long renderPass = this.getRenderPass();

            List<Long> imageViews = this.getImageViews();
            LongBuffer attachments = stack.mallocLong(imageViews.size());
            for (int i = 0; i < imageViews.size(); i++) {
                attachments.put(i, imageViews.get(i));
            }

            LongBuffer pFramebuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(extent.width());
            framebufferInfo.height(extent.height());
            framebufferInfo.layers(1);
            framebufferInfo.pAttachments(attachments);

            if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a framebuffer");
            }

            this.getFramebuffers().add(pFramebuffer.get(0));
        }
    }

    public void compile(
            int colorImageFormat,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue,
            int descriptorCount) {
        this.extent = extent;

        if (isContainer) {
            return;
        }

        this.createTextureSamplers();
        this.createUniformBuffers(descriptorCount);
        this.createRenderPass(colorImageFormat);
        this.createDescriptorSetLayouts();
        this.createDescriptorPools(descriptorCount);
        this.createDescriptorSets(descriptorCount, commandPool, graphicsQueue);
        this.createGraphicsPipelines();
        this.createImages(commandPool, graphicsQueue, colorImageFormat);
        this.createFramebuffers();
    }

    public void recreate(
            int colorImageFormat,
            VkExtent2D extent,
            long commandPool,
            VkQueue graphicsQueue) {
        this.extent = extent;

        if (isContainer) {
            return;
        }

        this.cleanup(true);

        this.createRenderPass(colorImageFormat);
        this.createGraphicsPipelines();
        this.createImages(commandPool, graphicsQueue, colorImageFormat);
        this.createFramebuffers();
    }

    public void cleanup(boolean reserveForRecreation) {
        if (isContainer) {
            return;
        }

        graphicsPipelines.forEach(graphicsPipeline -> vkDestroyPipeline(device, graphicsPipeline, null));
        pipelineLayouts.forEach(pipelineLayout -> vkDestroyPipelineLayout(device, pipelineLayout, null));
        graphicsPipelines.clear();
        pipelineLayouts.clear();

        images.forEach(image -> vkDestroyImage(device, image, null));
        imageMemories.forEach(imageMemory -> vkFreeMemory(device, imageMemory, null));
        imageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));
        framebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));
        images.clear();
        imageMemories.clear();
        imageViews.clear();
        framebuffers.clear();

        if (!reserveForRecreation) {
            vkDestroyImage(device, dummyImage, null);
            vkFreeMemory(device, dummyImageMemory, null);
            vkDestroyImageView(device, dummyImageView, null);

            vertShaderModules.forEach(vertShaderModule -> vkDestroyShaderModule(device, vertShaderModule, null));
            fragShaderModules.forEach(fragShaderModule -> vkDestroyShaderModule(device, fragShaderModule, null));
            vertShaderModules.clear();
            fragShaderModules.clear();

            textureSamplers.forEach(textureSampler -> vkDestroySampler(device, textureSampler, null));
            textureSamplers.clear();

            uniformBuffers.forEach(uniformBuffer -> vkDestroyBuffer(device, uniformBuffer, null));
            uniformBufferMemories.forEach(uniformBufferMemory -> vkFreeMemory(device, uniformBufferMemory, null));
            uniformBuffers.clear();
            uniformBufferMemories.clear();

            descriptorSetLayouts.forEach(
                    descriptorSetLayout -> vkDestroyDescriptorSetLayout(device, descriptorSetLayout, null));
            descriptorSetLayouts.clear();

            descriptorPools.forEach(descriptorPool -> vkDestroyDescriptorPool(device, descriptorPool, null));
            descriptorPools.clear();

            descriptorSets.clear();
        }

        vkDestroyRenderPass(device, renderPass, null);
    }

    protected VkDevice getDevice() {
        return device;
    }

    public int getMsaaSamples() {
        return msaaSamples;
    }

    public VkExtent2D getExtent() {
        return extent;
    }

    public boolean isContainer() {
        return isContainer;
    }

    public long getTextureSampler(int index) {
        return textureSamplers.get(index);
    }

    protected List<Long> getTextureSamplers() {
        return textureSamplers;
    }

    public long getUniformBuffer(int index) {
        return uniformBuffers.get(index);
    }

    protected List<Long> getUniformBuffers() {
        return uniformBuffers;
    }

    public long getUniformBufferMemory(int index) {
        return uniformBufferMemories.get(index);
    }

    protected List<Long> getUniformBufferMemories() {
        return uniformBufferMemories;
    }

    public long getRenderPass() {
        return renderPass;
    }

    public long getRenderPass(int index) {
        return -1;
    }

    protected void setRenderPass(long renderPass) {
        this.renderPass = renderPass;
    }

    public long getDescriptorSetLayout(int index) {
        return descriptorSetLayouts.get(index);
    }

    protected List<Long> getDescriptorSetLayouts() {
        return descriptorSetLayouts;
    }

    protected LongBuffer pDescriptorSetLayouts() {
        LongBuffer pLayouts = MemoryStack.stackGet().mallocLong(descriptorSetLayouts.size());
        for (int i = 0; i < descriptorSetLayouts.size(); i++) {
            pLayouts.put(i, descriptorSetLayouts.get(i));
        }

        return pLayouts;
    }

    public long getDescriptorPool(int index) {
        return descriptorPools.get(index);
    }

    protected List<Long> getDescriptorPools() {
        return descriptorPools;
    }

    public int getNumDescriptorSets() {
        return descriptorSets.size();
    }

    public long getDescriptorSet(int index) {
        return descriptorSets.get(index);
    }

    protected List<Long> getDescriptorSets() {
        return descriptorSets;
    }

    public LongBuffer pDescriptorSets() {
        LongBuffer pSets = MemoryStack.stackGet().mallocLong(descriptorSets.size());
        for (int i = 0; i < descriptorSets.size(); i++) {
            pSets.put(i, descriptorSets.get(i));
        }

        return pSets;
    }

    public long getVertShaderModule(int index) {
        return vertShaderModules.get(index);
    }

    protected List<Long> getVertShaderModules() {
        return vertShaderModules;
    }

    public long getFragShaderModule(int index) {
        return fragShaderModules.get(index);
    }

    protected List<Long> getFragShaderModules() {
        return fragShaderModules;
    }

    public long getPipelineLayout(int index) {
        return pipelineLayouts.get(index);
    }

    protected List<Long> getPipelineLayouts() {
        return pipelineLayouts;
    }

    public long getGraphicsPipeline(int index) {
        return graphicsPipelines.get(index);
    }

    protected List<Long> getGraphicsPipelines() {
        return graphicsPipelines;
    }

    public long getImage(int index) {
        return images.get(index);
    }

    protected List<Long> getImages() {
        return images;
    }

    public long getImageMemory(int index) {
        return imageMemories.get(index);
    }

    protected List<Long> getImageMemories() {
        return imageMemories;
    }

    public long getImageView(int index) {
        return imageViews.get(index);
    }

    protected List<Long> getImageViews() {
        return imageViews;
    }

    public int getNumFramebuffers() {
        return framebuffers.size();
    }

    public long getFramebuffer(int index) {
        return framebuffers.get(index);
    }

    protected List<Long> getFramebuffers() {
        return framebuffers;
    }

    public int getSetCount() {
        return setCount;
    }

    protected void setSetCount(int setCount) {
        this.setCount = setCount;
    }

    protected long createShaderModule(VkDevice device, ByteBuffer spirvCode) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkShaderModuleCreateInfo createInfo = VkShaderModuleCreateInfo.callocStack(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_SHADER_MODULE_CREATE_INFO);
            createInfo.pCode(spirvCode);

            LongBuffer pShaderModule = stack.mallocLong(1);
            if (vkCreateShaderModule(device, createInfo, null, pShaderModule) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a shader module");
            }

            return pShaderModule.get(0);
        }
    }
}
