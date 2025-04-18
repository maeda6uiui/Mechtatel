package com.github.maeda6uiui.mechtatel.core.vulkan.nabor.gbuffer;

import com.github.maeda6uiui.mechtatel.core.model.AssimpModelLoader;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.Nabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttVertex;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.AnimationUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.CameraUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.DepthResourceUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

class AlbedoNabor extends Nabor {
    public static final int MAX_NUM_TEXTURES = 1024;

    private int depthImageFormat;
    private int depthImageAspect;

    public static final int DEPTH_ATTACHMENT_INDEX = 0;
    public static final int ALBEDO_ATTACHMENT_INDEX = 1;
    public static final int ALBEDO_RESOLVE_ATTACHMENT_INDEX = 2;

    public AlbedoNabor(
            VkDevice device,
            int msaaSamples,
            int depthImageFormat,
            URL vertShaderResource,
            URL fragShaderResource) {
        super(device, msaaSamples, false, vertShaderResource, fragShaderResource);

        this.depthImageFormat = depthImageFormat;
        depthImageAspect = VK_IMAGE_ASPECT_DEPTH_BIT;
        if (DepthResourceUtils.hasStencilComponent(depthImageFormat)) {
            depthImageAspect |= VK_IMAGE_ASPECT_STENCIL_BIT;
        }
    }

    public void transitionDepthImageLayout(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        long depthImage = this.getImage(DEPTH_ATTACHMENT_INDEX);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                depthImage,
                depthImageAspect,
                VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1);
    }

    public long getDepthImageView() {
        return this.getImageView(DEPTH_ATTACHMENT_INDEX);
    }

    public void transitionAlbedoImageLayout(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        int msaaSamples = this.getMsaaSamples();

        long albedoImage;
        if (msaaSamples == VK_SAMPLE_COUNT_1_BIT) {
            albedoImage = this.getImage(ALBEDO_ATTACHMENT_INDEX);
        } else {
            albedoImage = this.getImage(ALBEDO_RESOLVE_ATTACHMENT_INDEX);
        }

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                albedoImage,
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1
        );
    }

    public long getAlbedoImageView() {
        int msaaSamples = this.getMsaaSamples();
        if (msaaSamples == VK_SAMPLE_COUNT_1_BIT) {
            return this.getImageView(ALBEDO_ATTACHMENT_INDEX);
        } else {
            return this.getImageView(ALBEDO_RESOLVE_ATTACHMENT_INDEX);
        }
    }

    @Override
    public void cleanup(boolean reserveForRecreation) {
        super.cleanup(reserveForRecreation);
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

        var animationUBOInfos = BufferUtils.createUBOBuffers(
                device, descriptorCount, AnimationUBO.SIZEOF);
        for (var animationUBOInfo : animationUBOInfos) {
            this.getUniformBuffers().add(animationUBOInfo.buffer);
            this.getUniformBufferMemories().add(animationUBOInfo.bufferMemory);
        }
    }

    private long createNonMSAARenderPass(int colorImageFormat) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(2, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.calloc(2, stack);

            //Depth-stencil attachment
            VkAttachmentDescription depthAttachment = attachments.get(DEPTH_ATTACHMENT_INDEX);
            depthAttachment.format(depthImageFormat);
            depthAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference depthAttachmentRef = attachmentRefs.get(DEPTH_ATTACHMENT_INDEX);
            depthAttachmentRef.attachment(DEPTH_ATTACHMENT_INDEX);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            //Albedo attachment
            VkAttachmentDescription albedoAttachment = attachments.get(ALBEDO_ATTACHMENT_INDEX);
            albedoAttachment.format(colorImageFormat);
            albedoAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            albedoAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            albedoAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            albedoAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            albedoAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            albedoAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            albedoAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference albedoAttachmentRef = attachmentRefs.get(ALBEDO_ATTACHMENT_INDEX);
            albedoAttachmentRef.attachment(ALBEDO_ATTACHMENT_INDEX);
            albedoAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pDepthStencilAttachment(depthAttachmentRef);
            subpass.pColorAttachments(VkAttachmentReference.calloc(1, stack).put(0, albedoAttachmentRef));

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

            return pRenderPass.get(0);
        }
    }

    private long createMSAARenderPass(int colorImageFormat, int msaaSamples) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(3, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.calloc(3, stack);

            //Depth-stencil attachment
            VkAttachmentDescription depthAttachment = attachments.get(DEPTH_ATTACHMENT_INDEX);
            depthAttachment.format(depthImageFormat);
            depthAttachment.samples(msaaSamples);
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference depthAttachmentRef = attachmentRefs.get(DEPTH_ATTACHMENT_INDEX);
            depthAttachmentRef.attachment(DEPTH_ATTACHMENT_INDEX);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            //Albedo attachment
            VkAttachmentDescription albedoAttachment = attachments.get(ALBEDO_ATTACHMENT_INDEX);
            albedoAttachment.format(colorImageFormat);
            albedoAttachment.samples(msaaSamples);
            albedoAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            albedoAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            albedoAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            albedoAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            albedoAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            albedoAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference albedoAttachmentRef = attachmentRefs.get(ALBEDO_ATTACHMENT_INDEX);
            albedoAttachmentRef.attachment(ALBEDO_ATTACHMENT_INDEX);
            albedoAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            //Albedo resolve attachment
            VkAttachmentDescription albedoResolveAttachment = attachments.get(ALBEDO_RESOLVE_ATTACHMENT_INDEX);
            albedoResolveAttachment.format(colorImageFormat);
            albedoResolveAttachment.samples(VK_SAMPLE_COUNT_1_BIT);
            albedoResolveAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            albedoResolveAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            albedoResolveAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            albedoResolveAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            albedoResolveAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            albedoResolveAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference albedoResolveAttachmentRef = attachmentRefs.get(ALBEDO_RESOLVE_ATTACHMENT_INDEX);
            albedoResolveAttachmentRef.attachment(ALBEDO_RESOLVE_ATTACHMENT_INDEX);
            albedoResolveAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pDepthStencilAttachment(depthAttachmentRef);
            subpass.pColorAttachments(VkAttachmentReference.calloc(1, stack).put(0, albedoAttachmentRef));
            subpass.pResolveAttachments(VkAttachmentReference.calloc(1, stack).put(0, albedoResolveAttachmentRef));

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

            return pRenderPass.get(0);
        }
    }

    @Override
    protected void createRenderPass(int colorImageFormat) {
        int msaaSamples = this.getMsaaSamples();

        long renderPass;
        if (msaaSamples == VK_SAMPLE_COUNT_1_BIT) {
            renderPass = this.createNonMSAARenderPass(colorImageFormat);
        } else {
            renderPass = this.createMSAARenderPass(colorImageFormat, msaaSamples);
        }

        this.setRenderPass(renderPass);
    }

    @Override
    protected void createDescriptorSetLayouts() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            //=== set 0 ===
            VkDescriptorSetLayoutBinding.Buffer uboLayoutBindings = VkDescriptorSetLayoutBinding.calloc(2, stack);

            VkDescriptorSetLayoutBinding cameraUBOLayoutBinding = uboLayoutBindings.get(0);
            cameraUBOLayoutBinding.binding(0);
            cameraUBOLayoutBinding.descriptorCount(1);
            cameraUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBOLayoutBinding.pImmutableSamplers(null);
            cameraUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

            VkDescriptorSetLayoutBinding animationUBOLayoutBinding = uboLayoutBindings.get(1);
            animationUBOLayoutBinding.binding(1);
            animationUBOLayoutBinding.descriptorCount(1);
            animationUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            animationUBOLayoutBinding.pImmutableSamplers(null);
            animationUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

            //=== set 1 ===
            VkDescriptorSetLayoutBinding.Buffer imageLayoutBindings = VkDescriptorSetLayoutBinding.calloc(1, stack);

            VkDescriptorSetLayoutBinding imageLayoutBinding = imageLayoutBindings.get(0);
            imageLayoutBinding.binding(0);
            imageLayoutBinding.descriptorCount(MAX_NUM_TEXTURES);
            imageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            imageLayoutBinding.pImmutableSamplers(null);
            imageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

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
            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.calloc(2, stack);

            VkDescriptorPoolSize cameraUBOPoolSize = uboPoolSizes.get(0);
            cameraUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            cameraUBOPoolSize.descriptorCount(descriptorCount);

            VkDescriptorPoolSize animationUBOPoolSize = uboPoolSizes.get(1);
            animationUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            animationUBOPoolSize.descriptorCount(descriptorCount * AssimpModelLoader.MAX_NUM_BONES);

            //=== set 1 ===
            VkDescriptorPoolSize.Buffer imagePoolSizes = VkDescriptorPoolSize.calloc(1, stack);

            VkDescriptorPoolSize imagePoolSize = imagePoolSizes.get(0);
            imagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            imagePoolSize.descriptorCount(descriptorCount * MAX_NUM_TEXTURES);

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
            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.calloc(2, stack);

            VkDescriptorBufferInfo cameraUBOInfo = uboInfos.get(0);
            cameraUBOInfo.buffer(this.getUniformBuffer(0));
            cameraUBOInfo.offset(0);
            cameraUBOInfo.range(CameraUBO.SIZEOF);

            VkDescriptorBufferInfo animationUBOInfo = uboInfos.get(1);
            animationUBOInfo.buffer(this.getUniformBuffer(1));
            animationUBOInfo.offset(0);
            animationUBOInfo.range(AnimationUBO.SIZEOF);

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(0);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(2);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            //=== set 1 ===
            VkDescriptorImageInfo.Buffer imageInfos = VkDescriptorImageInfo.calloc(MAX_NUM_TEXTURES, stack);
            for (int i = 0; i < MAX_NUM_TEXTURES; i++) {
                VkDescriptorImageInfo imageInfo = imageInfos.get(i);
                imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                imageInfo.imageView(this.getDummyImageView());
            }

            VkWriteDescriptorSet imageDescriptorWrite = descriptorWrites.get(1);
            imageDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            imageDescriptorWrite.dstBinding(0);
            imageDescriptorWrite.dstArrayElement(0);
            imageDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            imageDescriptorWrite.descriptorCount(MAX_NUM_TEXTURES);
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

    private long createGraphicsPipeline(int cullMode, boolean depthTestEnable) {
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
            vertexInputInfo.pVertexBindingDescriptions(VkMttVertex.getBindingDescription());
            vertexInputInfo.pVertexAttributeDescriptions(VkMttVertex.getAttributeDescriptions());

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
            rasterizer.cullMode(cullMode);
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
            depthStencil.depthTestEnable(depthTestEnable);
            depthStencil.depthWriteEnable(true);
            depthStencil.depthCompareOp(VK_COMPARE_OP_LESS);
            depthStencil.depthBoundsTestEnable(false);
            depthStencil.minDepthBounds(0.0f);
            depthStencil.maxDepthBounds(1.0f);
            depthStencil.stencilTestEnable(false);

            //Color blending
            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachments
                    = VkPipelineColorBlendAttachmentState.calloc(1, stack);
            VkPipelineColorBlendAttachmentState colorBlendAttachment = colorBlendAttachments.get(0);
            colorBlendAttachment.colorWriteMask(
                    VK_COLOR_COMPONENT_R_BIT |
                            VK_COLOR_COMPONENT_G_BIT |
                            VK_COLOR_COMPONENT_B_BIT |
                            VK_COLOR_COMPONENT_A_BIT);
            colorBlendAttachment.blendEnable(true);
            colorBlendAttachment.srcColorBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA);
            colorBlendAttachment.dstColorBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
            colorBlendAttachment.colorBlendOp(VK_BLEND_OP_ADD);
            colorBlendAttachment.srcAlphaBlendFactor(VK_BLEND_FACTOR_SRC_ALPHA);
            colorBlendAttachment.dstAlphaBlendFactor(VK_BLEND_FACTOR_ONE_MINUS_SRC_ALPHA);
            colorBlendAttachment.alphaBlendOp(VK_BLEND_OP_SUBTRACT);

            VkPipelineColorBlendStateCreateInfo colorBlending = VkPipelineColorBlendStateCreateInfo.calloc(stack);
            colorBlending.sType(VK_STRUCTURE_TYPE_PIPELINE_COLOR_BLEND_STATE_CREATE_INFO);
            colorBlending.logicOpEnable(false);
            colorBlending.logicOp(VK_LOGIC_OP_COPY);
            colorBlending.pAttachments(colorBlendAttachments);
            colorBlending.blendConstants(stack.floats(0.0f, 0.0f, 0.0f, 0.0f));

            //Push constants
            VkPushConstantRange.Buffer pushConstants = VkPushConstantRange.calloc(2, stack);

            VkPushConstantRange vertPC = pushConstants.get(0);
            vertPC.offset(0);
            vertPC.size(1 * 16 * Float.BYTES + 1 * Integer.BYTES);
            vertPC.stageFlags(VK_SHADER_STAGE_VERTEX_BIT);

            VkPushConstantRange fragPC = pushConstants.get(1);
            fragPC.offset(1 * 16 * Float.BYTES + 1 * Integer.BYTES);
            fragPC.size(1 * Integer.BYTES);
            fragPC.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //Pipeline layout creation
            VkPipelineLayoutCreateInfo pipelineLayoutInfo = VkPipelineLayoutCreateInfo.calloc(stack);
            pipelineLayoutInfo.sType(VK_STRUCTURE_TYPE_PIPELINE_LAYOUT_CREATE_INFO);
            pipelineLayoutInfo.pSetLayouts(this.pDescriptorSetLayouts());
            pipelineLayoutInfo.pPushConstantRanges(pushConstants);

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
            if (vkCreateGraphicsPipelines(
                    device, VK_NULL_HANDLE, pipelineInfo, null, pGraphicsPipeline) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a graphics pipeline");
            }

            return pGraphicsPipeline.get(0);
        }
    }

    @Override
    protected void createGraphicsPipelines() {
        long normalGraphicsPipeline = this.createGraphicsPipeline(VK_CULL_MODE_BACK_BIT, true);
        long imguiGraphicsPipeline = this.createGraphicsPipeline(VK_CULL_MODE_NONE, false);

        this.getGraphicsPipelines().add(normalGraphicsPipeline);
        this.getGraphicsPipelines().add(imguiGraphicsPipeline);
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

            //Depth image
            ImageUtils.createImage(
                    device,
                    extent.width(),
                    extent.height(),
                    1,
                    msaaSamples,
                    depthImageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            long depthImage = pImage.get(0);
            long depthImageMemory = pImageMemory.get(0);

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.image(depthImage);
            viewInfo.format(depthImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(1);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            long depthImageView = pImageView.get(0);

            this.getImages().add(depthImage);
            this.getImageMemories().add(depthImageMemory);
            this.getImageViews().add(depthImageView);

            //Albedo image
            ImageUtils.createImage(
                    device,
                    extent.width(),
                    extent.height(),
                    1,
                    msaaSamples,
                    colorImageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            long albedoImage = pImage.get(0);
            long albedoImageMemory = pImageMemory.get(0);

            viewInfo.image(albedoImage);
            viewInfo.format(colorImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            long albedoImageView = pImageView.get(0);

            this.getImages().add(albedoImage);
            this.getImageMemories().add(albedoImageMemory);
            this.getImageViews().add(albedoImageView);

            //Albedo resolve image
            if (msaaSamples != VK_SAMPLE_COUNT_1_BIT) {
                ImageUtils.createImage(
                        device,
                        extent.width(),
                        extent.height(),
                        1,
                        VK_SAMPLE_COUNT_1_BIT,
                        colorImageFormat,
                        VK_IMAGE_TILING_OPTIMAL,
                        VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                        VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                        pImage,
                        pImageMemory);
                long albedoResolveImage = pImage.get(0);
                long albedoResolveImageMemory = pImageMemory.get(0);

                viewInfo.image(albedoResolveImage);
                viewInfo.format(colorImageFormat);
                viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

                if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create an image view");
                }
                long albedoResolveImageView = pImageView.get(0);

                this.getImages().add(albedoResolveImage);
                this.getImageMemories().add(albedoResolveImageMemory);
                this.getImageViews().add(albedoResolveImageView);
            }
        }
    }
}
