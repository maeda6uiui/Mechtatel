package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttVertex2D;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.MergeScenesInfoUBO;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_INT;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Nabor to merge scenes by comparing the depths
 *
 * @author maeda6uiui
 */
public class MergeScenesNabor extends Nabor {
    public static final int MAX_NUM_SCENES = 8;

    private int depthImageFormat;
    private int positionImageFormat;
    private int normalImageFormat;
    private int stencilImageFormat;

    public static final int ALBEDO_ATTACHMENT_INDEX = 0;
    public static final int DEPTH_ATTACHMENT_INDEX = 1;
    public static final int POSITION_ATTACHMENT_INDEX = 2;
    public static final int NORMAL_ATTACHMENT_INDEX = 3;
    public static final int STENCIL_ATTACHMENT_INDEX = 4;

    public MergeScenesNabor(
            VkDevice device,
            int depthImageFormat,
            int positionImageFormat,
            int normalImageFormat,
            int stencilImageFormat,
            URL vertShaderResource,
            URL fragShaderResource) {
        super(device, VK_SAMPLE_COUNT_1_BIT, false, vertShaderResource, fragShaderResource);

        this.depthImageFormat = depthImageFormat;
        this.positionImageFormat = positionImageFormat;
        this.normalImageFormat = normalImageFormat;
        this.stencilImageFormat = stencilImageFormat;
    }

    public void transitionAlbedoImageLayout(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        long albedoImage = this.getImage(ALBEDO_ATTACHMENT_INDEX);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                albedoImage,
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1);
    }

    public long getAlbedoImageView() {
        return this.getImageView(ALBEDO_ATTACHMENT_INDEX);
    }

    public void transitionDepthImageLayout(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        long depthImage = this.getImage(DEPTH_ATTACHMENT_INDEX);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                depthImage,
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1);
    }

    public long getDepthImageView() {
        return this.getImageView(DEPTH_ATTACHMENT_INDEX);
    }

    public void transitionPositionImageLayout(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        long positionImage = this.getImage(POSITION_ATTACHMENT_INDEX);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                positionImage,
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1);
    }

    public long getPositionImageView() {
        return this.getImageView(POSITION_ATTACHMENT_INDEX);
    }

    public void transitionNormalImageLayout(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        long normalImage = this.getImage(NORMAL_ATTACHMENT_INDEX);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                normalImage,
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1);
    }

    public long getNormalImageView() {
        return this.getImageView(NORMAL_ATTACHMENT_INDEX);
    }

    public void transitionStencilImageLayout(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        long stencilImage = this.getImage(STENCIL_ATTACHMENT_INDEX);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                stencilImage,
                VK_IMAGE_ASPECT_COLOR_BIT,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1);
    }

    public long getStencilImageView() {
        return this.getImageView(STENCIL_ATTACHMENT_INDEX);
    }

    @Override
    public void cleanup(boolean reserveForRecreation) {
        super.cleanup(reserveForRecreation);
    }

    @Override
    protected void createUniformBuffers(int descriptorCount) {
        VkDevice device = this.getDevice();

        var mergeScenesInfoUBOInfos = BufferUtils.createUBOBuffers(device, descriptorCount, SIZEOF_INT);
        for (var mergeScenesInfoUBOInfo : mergeScenesInfoUBOInfos) {
            this.getUniformBuffers().add(mergeScenesInfoUBOInfo.buffer);
            this.getUniformBufferMemories().add(mergeScenesInfoUBOInfo.bufferMemory);
        }
    }

    @Override
    protected void createRenderPass(int colorImageFormat) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();
            int msaaSamples = this.getMsaaSamples();

            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.calloc(5, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.calloc(5, stack);

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

            //Depth attachment
            //In this nabor, depth is supposed to be passed in a normal color format (colorImageFormat).
            //Therefore, there is no depth attachment in this render pass.
            VkAttachmentDescription depthAttachment = attachments.get(DEPTH_ATTACHMENT_INDEX);
            depthAttachment.format(depthImageFormat);
            depthAttachment.samples(msaaSamples);
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference depthAttachmentRef = attachmentRefs.get(DEPTH_ATTACHMENT_INDEX);
            depthAttachmentRef.attachment(DEPTH_ATTACHMENT_INDEX);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            //Position attachment
            VkAttachmentDescription positionAttachment = attachments.get(POSITION_ATTACHMENT_INDEX);
            positionAttachment.format(positionImageFormat);
            positionAttachment.samples(msaaSamples);
            positionAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            positionAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            positionAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            positionAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            positionAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            positionAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference positionAttachmentRef = attachmentRefs.get(POSITION_ATTACHMENT_INDEX);
            positionAttachmentRef.attachment(POSITION_ATTACHMENT_INDEX);
            positionAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            //Normal attachment
            VkAttachmentDescription normalAttachment = attachments.get(NORMAL_ATTACHMENT_INDEX);
            normalAttachment.format(normalImageFormat);
            normalAttachment.samples(msaaSamples);
            normalAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            normalAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            normalAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            normalAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            normalAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            normalAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference normalAttachmentRef = attachmentRefs.get(NORMAL_ATTACHMENT_INDEX);
            normalAttachmentRef.attachment(NORMAL_ATTACHMENT_INDEX);
            normalAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            //Stencil attachment
            VkAttachmentDescription stencilAttachment = attachments.get(STENCIL_ATTACHMENT_INDEX);
            stencilAttachment.format(stencilImageFormat);
            stencilAttachment.samples(msaaSamples);
            stencilAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            stencilAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            stencilAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            stencilAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            stencilAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            stencilAttachment.finalLayout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference stencilAttachmentRef = attachmentRefs.get(STENCIL_ATTACHMENT_INDEX);
            stencilAttachmentRef.attachment(STENCIL_ATTACHMENT_INDEX);
            stencilAttachmentRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkAttachmentReference.Buffer colorAttachmentRefs = VkAttachmentReference.calloc(5, stack);
            colorAttachmentRefs.put(ALBEDO_ATTACHMENT_INDEX, albedoAttachmentRef);
            colorAttachmentRefs.put(DEPTH_ATTACHMENT_INDEX, depthAttachmentRef);
            colorAttachmentRefs.put(POSITION_ATTACHMENT_INDEX, positionAttachmentRef);
            colorAttachmentRefs.put(NORMAL_ATTACHMENT_INDEX, normalAttachmentRef);
            colorAttachmentRefs.put(STENCIL_ATTACHMENT_INDEX, stencilAttachmentRef);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.calloc(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(5);
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
    protected void createDescriptorSetLayouts() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            //=== set 0 ===
            VkDescriptorSetLayoutBinding.Buffer imageLayoutBindings = VkDescriptorSetLayoutBinding.calloc(5, stack);

            VkDescriptorSetLayoutBinding albedoImageLayoutBinding = imageLayoutBindings.get(0);
            albedoImageLayoutBinding.binding(0);
            albedoImageLayoutBinding.descriptorCount(MAX_NUM_SCENES);
            albedoImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            albedoImageLayoutBinding.pImmutableSamplers(null);
            albedoImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding depthImageLayoutBinding = imageLayoutBindings.get(1);
            depthImageLayoutBinding.binding(1);
            depthImageLayoutBinding.descriptorCount(MAX_NUM_SCENES);
            depthImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            depthImageLayoutBinding.pImmutableSamplers(null);
            depthImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding positionImageLayoutBinding = imageLayoutBindings.get(2);
            positionImageLayoutBinding.binding(2);
            positionImageLayoutBinding.descriptorCount(MAX_NUM_SCENES);
            positionImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            positionImageLayoutBinding.pImmutableSamplers(null);
            positionImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding normalImageLayoutBinding = imageLayoutBindings.get(3);
            normalImageLayoutBinding.binding(3);
            normalImageLayoutBinding.descriptorCount(MAX_NUM_SCENES);
            normalImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            normalImageLayoutBinding.pImmutableSamplers(null);
            normalImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            VkDescriptorSetLayoutBinding stencilImageLayoutBinding = imageLayoutBindings.get(4);
            stencilImageLayoutBinding.binding(4);
            stencilImageLayoutBinding.descriptorCount(MAX_NUM_SCENES);
            stencilImageLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            stencilImageLayoutBinding.pImmutableSamplers(null);
            stencilImageLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //=== set 1 ===
            VkDescriptorSetLayoutBinding.Buffer samplerLayoutBindings = VkDescriptorSetLayoutBinding.calloc(1, stack);

            VkDescriptorSetLayoutBinding samplerLayoutBinding = samplerLayoutBindings.get(0);
            samplerLayoutBinding.binding(0);
            samplerLayoutBinding.descriptorCount(1);
            samplerLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLER);
            samplerLayoutBinding.pImmutableSamplers(null);
            samplerLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //=== set 2 ===
            VkDescriptorSetLayoutBinding.Buffer uboLayoutBindings = VkDescriptorSetLayoutBinding.calloc(1, stack);

            VkDescriptorSetLayoutBinding mergeScenesInfoUBOLayoutBinding = uboLayoutBindings.get(0);
            mergeScenesInfoUBOLayoutBinding.binding(0);
            mergeScenesInfoUBOLayoutBinding.descriptorCount(1);
            mergeScenesInfoUBOLayoutBinding.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            mergeScenesInfoUBOLayoutBinding.pImmutableSamplers(null);
            mergeScenesInfoUBOLayoutBinding.stageFlags(VK_SHADER_STAGE_FRAGMENT_BIT);

            //Create descriptor set layouts
            VkDescriptorSetLayoutCreateInfo.Buffer layoutCreateInfos = VkDescriptorSetLayoutCreateInfo.calloc(3, stack);

            //=== set 0 ===
            VkDescriptorSetLayoutCreateInfo imageLayoutCreateInfo = layoutCreateInfos.get(0);
            imageLayoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            imageLayoutCreateInfo.pBindings(imageLayoutBindings);

            //=== set 1 ===
            VkDescriptorSetLayoutCreateInfo samplerLayoutCreateInfo = layoutCreateInfos.get(1);
            samplerLayoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            samplerLayoutCreateInfo.pBindings(samplerLayoutBindings);

            //=== set 2 ===
            VkDescriptorSetLayoutCreateInfo uboLayoutCreateInfo = layoutCreateInfos.get(2);
            uboLayoutCreateInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_SET_LAYOUT_CREATE_INFO);
            uboLayoutCreateInfo.pBindings(uboLayoutBindings);

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
            VkDescriptorPoolSize.Buffer imagePoolSizes = VkDescriptorPoolSize.calloc(5, stack);

            VkDescriptorPoolSize albedoImagePoolSize = imagePoolSizes.get(0);
            albedoImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            albedoImagePoolSize.descriptorCount(descriptorCount * MAX_NUM_SCENES);

            VkDescriptorPoolSize depthImagePoolSize = imagePoolSizes.get(1);
            depthImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            depthImagePoolSize.descriptorCount(descriptorCount * MAX_NUM_SCENES);

            VkDescriptorPoolSize positionImagePoolSize = imagePoolSizes.get(2);
            positionImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            positionImagePoolSize.descriptorCount(descriptorCount * MAX_NUM_SCENES);

            VkDescriptorPoolSize normalImagePoolSize = imagePoolSizes.get(3);
            normalImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            normalImagePoolSize.descriptorCount(descriptorCount * MAX_NUM_SCENES);

            VkDescriptorPoolSize stencilImagePoolSize = imagePoolSizes.get(4);
            stencilImagePoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            stencilImagePoolSize.descriptorCount(descriptorCount * MAX_NUM_SCENES);

            //=== set 1 ===
            VkDescriptorPoolSize.Buffer samplerPoolSizes = VkDescriptorPoolSize.calloc(1, stack);

            VkDescriptorPoolSize samplerPoolSize = samplerPoolSizes.get(0);
            samplerPoolSize.type(VK_DESCRIPTOR_TYPE_SAMPLER);
            samplerPoolSize.descriptorCount(descriptorCount);

            //=== set 2 ===
            VkDescriptorPoolSize.Buffer uboPoolSizes = VkDescriptorPoolSize.calloc(1, stack);

            VkDescriptorPoolSize mergeScenesInfoUBOPoolSize = uboPoolSizes.get(0);
            mergeScenesInfoUBOPoolSize.type(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            mergeScenesInfoUBOPoolSize.descriptorCount(descriptorCount);

            //Create descriptor pools
            VkDescriptorPoolCreateInfo.Buffer poolInfos = VkDescriptorPoolCreateInfo.calloc(3, stack);

            //=== set 0 ===
            VkDescriptorPoolCreateInfo imagePoolInfo = poolInfos.get(0);
            imagePoolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            imagePoolInfo.pPoolSizes(imagePoolSizes);
            imagePoolInfo.maxSets(descriptorCount);

            //=== set 1 ===
            VkDescriptorPoolCreateInfo samplerPoolInfo = poolInfos.get(1);
            samplerPoolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            samplerPoolInfo.pPoolSizes(samplerPoolSizes);
            samplerPoolInfo.maxSets(descriptorCount);

            //=== set 2 ===
            VkDescriptorPoolCreateInfo uboPoolInfo = poolInfos.get(2);
            uboPoolInfo.sType(VK_STRUCTURE_TYPE_DESCRIPTOR_POOL_CREATE_INFO);
            uboPoolInfo.pPoolSizes(uboPoolSizes);
            uboPoolInfo.maxSets(descriptorCount);

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
            VkDescriptorImageInfo.Buffer imageInfos = VkDescriptorImageInfo.calloc(MAX_NUM_SCENES * 5, stack);
            for (int i = 0; i < MAX_NUM_SCENES * 5; i++) {
                VkDescriptorImageInfo imageInfo = imageInfos.get(i);
                imageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                imageInfo.imageView(this.getDummyImageView());
            }

            VkWriteDescriptorSet imageDescriptorWrite = descriptorWrites.get(0);
            imageDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            imageDescriptorWrite.dstBinding(0);
            imageDescriptorWrite.dstArrayElement(0);
            imageDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            imageDescriptorWrite.descriptorCount(MAX_NUM_SCENES * 5);
            imageDescriptorWrite.pImageInfo(imageInfos);

            //=== set 1 ===
            VkDescriptorImageInfo.Buffer samplerInfos = VkDescriptorImageInfo.calloc(1, stack);

            VkDescriptorImageInfo samplerInfo = samplerInfos.get(0);
            samplerInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            samplerInfo.sampler(this.getTextureSampler(0));

            VkWriteDescriptorSet samplerDescriptorWrite = descriptorWrites.get(1);
            samplerDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            samplerDescriptorWrite.dstBinding(0);
            samplerDescriptorWrite.dstArrayElement(0);
            samplerDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLER);
            samplerDescriptorWrite.descriptorCount(1);
            samplerDescriptorWrite.pImageInfo(samplerInfos);

            //=== set 2 ===
            VkDescriptorBufferInfo.Buffer uboInfos = VkDescriptorBufferInfo.calloc(1, stack);

            VkDescriptorBufferInfo mergeScenesInfoUBOInfo = uboInfos.get(0);
            mergeScenesInfoUBOInfo.buffer(this.getUniformBuffer(0));
            mergeScenesInfoUBOInfo.offset(0);
            mergeScenesInfoUBOInfo.range(MergeScenesInfoUBO.SIZEOF);

            VkWriteDescriptorSet uboDescriptorWrite = descriptorWrites.get(2);
            uboDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            uboDescriptorWrite.dstBinding(0);
            uboDescriptorWrite.dstArrayElement(0);
            uboDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_UNIFORM_BUFFER);
            uboDescriptorWrite.descriptorCount(1);
            uboDescriptorWrite.pBufferInfo(uboInfos);

            for (int i = 0; i < descriptorCount; i++) {
                imageDescriptorWrite.dstSet(descriptorSets.get(i));
                samplerDescriptorWrite.dstSet(descriptorSets.get(i + descriptorCount));
                uboDescriptorWrite.dstSet(descriptorSets.get(i + descriptorCount * 2));

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
            vertexInputInfo.pVertexBindingDescriptions(VkMttVertex2D.getBindingDescription());
            vertexInputInfo.pVertexAttributeDescriptions(VkMttVertex2D.getAttributeDescriptions());

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
            VkPipelineColorBlendAttachmentState.Buffer colorBlendAttachments
                    = VkPipelineColorBlendAttachmentState.calloc(5, stack);
            for (int i = 0; i < 5; i++) {
                VkPipelineColorBlendAttachmentState colorBlendAttachment = colorBlendAttachments.get(i);
                colorBlendAttachment.colorWriteMask(
                        VK_COLOR_COMPONENT_R_BIT |
                                VK_COLOR_COMPONENT_G_BIT |
                                VK_COLOR_COMPONENT_B_BIT |
                                VK_COLOR_COMPONENT_A_BIT);
                colorBlendAttachment.blendEnable(false);
            }

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

            //Albedo image
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
            long albedoImage = pImage.get(0);
            long albedoImageMemory = pImageMemory.get(0);

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.image(albedoImage);
            viewInfo.format(colorImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(1);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            long albedoImageView = pImageView.get(0);

            this.getImages().add(albedoImage);
            this.getImageMemories().add(albedoImageMemory);
            this.getImageViews().add(albedoImageView);

            //Depth image
            ImageUtils.createImage(
                    device,
                    extent.width(),
                    extent.height(),
                    1,
                    msaaSamples,
                    depthImageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            long depthImage = pImage.get(0);
            long depthImageMemory = pImageMemory.get(0);

            viewInfo.image(depthImage);
            viewInfo.format(depthImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            long depthImageView = pImageView.get(0);

            this.getImages().add(depthImage);
            this.getImageMemories().add(depthImageMemory);
            this.getImageViews().add(depthImageView);

            //Position image
            ImageUtils.createImage(
                    device,
                    extent.width(),
                    extent.height(),
                    1,
                    msaaSamples,
                    positionImageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            long positionImage = pImage.get(0);
            long positionImageMemory = pImageMemory.get(0);

            viewInfo.image(positionImage);
            viewInfo.format(positionImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            long positionImageView = pImageView.get(0);

            this.getImages().add(positionImage);
            this.getImageMemories().add(positionImageMemory);
            this.getImageViews().add(positionImageView);

            //Normal image
            ImageUtils.createImage(
                    device,
                    extent.width(),
                    extent.height(),
                    1,
                    msaaSamples,
                    normalImageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            long normalImage = pImage.get(0);
            long normalImageMemory = pImageMemory.get(0);

            viewInfo.image(normalImage);
            viewInfo.format(normalImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            long normalImageView = pImageView.get(0);

            this.getImages().add(normalImage);
            this.getImageMemories().add(normalImageMemory);
            this.getImageViews().add(normalImageView);

            //Stencil image
            ImageUtils.createImage(
                    device,
                    extent.width(),
                    extent.height(),
                    1,
                    msaaSamples,
                    stencilImageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            long stencilImage = pImage.get(0);
            long stencilImageMemory = pImageMemory.get(0);

            viewInfo.image(stencilImage);
            viewInfo.format(stencilImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            long stencilImageView = pImageView.get(0);

            this.getImages().add(stencilImage);
            this.getImageMemories().add(stencilImageMemory);
            this.getImageViews().add(stencilImageView);
        }
    }

    public void bindAlbedoImages(VkCommandBuffer commandBuffer, List<Long> imageViews) {
        this.bindImages(commandBuffer, 0, ALBEDO_ATTACHMENT_INDEX, imageViews);
    }

    public void bindDepthImages(VkCommandBuffer commandBuffer, List<Long> imageViews) {
        this.bindImages(commandBuffer, 0, DEPTH_ATTACHMENT_INDEX, imageViews);
    }

    public void bindPositionImages(VkCommandBuffer commandBuffer, List<Long> imageViews) {
        this.bindImages(commandBuffer, 0, POSITION_ATTACHMENT_INDEX, imageViews);
    }

    public void bindNormalImages(VkCommandBuffer commandBuffer, List<Long> imageViews) {
        this.bindImages(commandBuffer, 0, NORMAL_ATTACHMENT_INDEX, imageViews);
    }

    public void bindStencilImages(VkCommandBuffer commandBuffer, List<Long> imageViews) {
        this.bindImages(commandBuffer, 0, STENCIL_ATTACHMENT_INDEX, imageViews);
    }
}
