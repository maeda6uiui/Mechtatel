package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Base class for post-processing nabors
 *
 * @author maeda
 */
public class PostProcessingNabor extends Nabor {
    private long dummyImage;
    private long dummyImageMemory;
    private long dummyImageView;

    public PostProcessingNabor(VkDevice device, int msaaSamples) {
        super(device, msaaSamples);
    }

    public long getDummyImageView() {
        return dummyImageView;
    }

    public void transitionColorImage(long commandPool, VkQueue graphicsQueue) {
        VkDevice device = this.getDevice();
        long colorImage = this.getImage(0);

        ImageUtils.transitionImageLayout(
                device,
                commandPool,
                graphicsQueue,
                colorImage,
                false,
                VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                1);
    }

    public long getColorImageView() {
        return this.getImageView(0);
    }

    @Override
    public void cleanup(boolean reserveForRecreation) {
        super.cleanup(reserveForRecreation);

        VkDevice device = this.getDevice();

        if (!reserveForRecreation) {
            vkDestroyImage(device, dummyImage, null);
            vkFreeMemory(device, dummyImageMemory, null);
            vkDestroyImageView(device, dummyImageView, null);
        }
    }

    @Override
    protected void createRenderPass(int colorImageFormat) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();
            int msaaSamples = this.getMsaaSamples();

            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(1, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.callocStack(1, stack);

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

            VkAttachmentReference.Buffer colorAttachmentRefs = VkAttachmentReference.callocStack(1, stack);
            colorAttachmentRefs.put(0, colorAttachmentRef);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(colorAttachmentRefs);

            VkSubpassDependency.Buffer dependency = VkSubpassDependency.callocStack(1, stack);
            dependency.srcSubpass(VK_SUBPASS_EXTERNAL);
            dependency.dstSubpass(0);
            dependency.srcStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.srcAccessMask(0);
            dependency.dstStageMask(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT);
            dependency.dstAccessMask(VK_ACCESS_COLOR_ATTACHMENT_READ_BIT | VK_ACCESS_COLOR_ATTACHMENT_WRITE_BIT);

            VkRenderPassCreateInfo renderPassInfo = VkRenderPassCreateInfo.callocStack(stack);
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

    @Override
    protected void createDescriptorSets(int descriptorCount, long commandPool, VkQueue graphicsQueue) {
        this.createDummyImage(commandPool, graphicsQueue);
    }

    @Override
    protected void createImages(
            long commandPool,
            VkQueue graphicsQueue,
            int colorImageFormat) {
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
                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pImage,
                    pImageMemory);
            long colorImage = pImage.get(0);
            long colorImageMemory = pImageMemory.get(0);

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
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

    @Override
    protected void createFramebuffers() {
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

    public void bindImages(
            VkCommandBuffer commandBuffer,
            long colorImageView,
            long depthImageView,
            long positionImageView,
            long normalImageView) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkDevice device = this.getDevice();

            VkDescriptorImageInfo.Buffer imageInfos = VkDescriptorImageInfo.callocStack(4, stack);

            VkDescriptorImageInfo colorImageInfo = imageInfos.get(0);
            colorImageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            colorImageInfo.imageView(colorImageView);

            VkDescriptorImageInfo depthImageInfo = imageInfos.get(1);
            depthImageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            depthImageInfo.imageView(depthImageView);

            VkDescriptorImageInfo positionImageInfo = imageInfos.get(2);
            positionImageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            positionImageInfo.imageView(positionImageView);

            VkDescriptorImageInfo normalImageInfo = imageInfos.get(3);
            normalImageInfo.imageLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            normalImageInfo.imageView(normalImageView);

            VkWriteDescriptorSet.Buffer imageDescriptorWrite = VkWriteDescriptorSet.callocStack(1, stack);
            imageDescriptorWrite.sType(VK_STRUCTURE_TYPE_WRITE_DESCRIPTOR_SET);
            imageDescriptorWrite.dstBinding(0);
            imageDescriptorWrite.dstArrayElement(0);
            imageDescriptorWrite.descriptorType(VK_DESCRIPTOR_TYPE_SAMPLED_IMAGE);
            imageDescriptorWrite.descriptorCount(4);
            imageDescriptorWrite.pImageInfo(imageInfos);

            long descriptorSet = this.getDescriptorSet(1);
            imageDescriptorWrite.dstSet(descriptorSet);

            vkUpdateDescriptorSets(device, imageDescriptorWrite, null);

            vkCmdBindDescriptorSets(
                    commandBuffer,
                    VK_PIPELINE_BIND_POINT_GRAPHICS,
                    this.getPipelineLayout(0),
                    0,
                    this.pDescriptorSets(),
                    null);
        }
    }
}