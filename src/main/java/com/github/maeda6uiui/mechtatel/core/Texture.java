package com.github.maeda6uiui.mechtatel.core;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Texture
 *
 * @author maeda
 */
class Texture {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private long textureImage;
    private long textureImageMemory;

    private long textureImageView;

    private void memcpy(ByteBuffer dst, ByteBuffer src, long size) {
        src.limit((int) size);
        dst.put(src);
        src.limit(src.capacity()).rewind();
    }

    private VkCommandBuffer beginSingleTimeCommands() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(commandPool);
            allocInfo.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer);
            var commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device);

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkBeginCommandBuffer(commandBuffer, beginInfo);

            return commandBuffer;
        }
    }

    private void endSingleTimeCommands(VkCommandBuffer commandBuffer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            vkEndCommandBuffer(commandBuffer);

            VkSubmitInfo.Buffer submitInfo = VkSubmitInfo.callocStack(1, stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(stack.pointers(commandBuffer));

            vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE);
            vkQueueWaitIdle(graphicsQueue);

            vkFreeCommandBuffers(device, commandPool, commandBuffer);
        }
    }

    private void copyBuffer(long srcBuffer, long dstBuffer, long size) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBuffer commandBuffer = this.beginSingleTimeCommands();

            VkBufferCopy.Buffer copyRegion = VkBufferCopy.callocStack(1, stack);
            copyRegion.size(size);

            vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);

            this.endSingleTimeCommands(commandBuffer);
        }
    }

    private void createImage(
            int width,
            int height,
            int format,
            int tiling,
            int usage,
            int memProperties,
            LongBuffer pTextureImage,
            LongBuffer pTextureImageMemory) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageCreateInfo imageInfo = VkImageCreateInfo.callocStack(stack);
            imageInfo.sType(VK_STRUCTURE_TYPE_IMAGE_CREATE_INFO);
            imageInfo.imageType(VK_IMAGE_TYPE_2D);
            imageInfo.extent().width(width);
            imageInfo.extent().height(height);
            imageInfo.extent().depth(1);
            imageInfo.mipLevels(1);
            imageInfo.arrayLayers(1);
            imageInfo.format(format);
            imageInfo.tiling(tiling);
            imageInfo.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            imageInfo.usage(usage);
            imageInfo.samples(VK_SAMPLE_COUNT_1_BIT);
            imageInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            if (vkCreateImage(device, imageInfo, null, pTextureImage) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image");
            }

            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetImageMemoryRequirements(device, pTextureImage.get(0), memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(MemoryUtils.findMemoryType(device, memRequirements.memoryTypeBits(), memProperties));

            if (vkAllocateMemory(device, allocInfo, null, pTextureImageMemory) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate an image memory");
            }

            vkBindImageMemory(device, pTextureImage.get(0), pTextureImageMemory.get(0), 0);
        }
    }

    private void transitionImageLayout(long image, int oldLayout, int newLayout) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.callocStack(1, stack);
            barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            barrier.oldLayout(oldLayout);
            barrier.newLayout(newLayout);
            barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.image(image);
            barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            barrier.subresourceRange().baseMipLevel(0);
            barrier.subresourceRange().levelCount(1);
            barrier.subresourceRange().baseArrayLayer(0);
            barrier.subresourceRange().layerCount(1);

            int sourceStage = 0;
            int destinationStage = 0;

            if (oldLayout == VK_IMAGE_LAYOUT_UNDEFINED && newLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL) {
                barrier.srcAccessMask(0);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);

                sourceStage = VK_PIPELINE_STAGE_TOP_OF_PIPE_BIT;
                destinationStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
            } else if (oldLayout == VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL && newLayout == VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL) {
                barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

                sourceStage = VK_PIPELINE_STAGE_TRANSFER_BIT;
                destinationStage = VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT;
            } else {
                throw new IllegalArgumentException("Unsupported layout transition");
            }

            VkCommandBuffer commandBuffer = this.beginSingleTimeCommands();

            vkCmdPipelineBarrier(
                    commandBuffer,
                    sourceStage,
                    destinationStage,
                    0,
                    null,
                    null,
                    barrier);

            this.endSingleTimeCommands(commandBuffer);
        }
    }

    private void copyBufferToImage(long buffer, long image, int width, int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBuffer commandBuffer = this.beginSingleTimeCommands();

            VkBufferImageCopy.Buffer region = VkBufferImageCopy.callocStack(1, stack);
            region.bufferOffset(0);
            region.bufferRowLength(0);
            region.bufferImageHeight(0);
            region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            region.imageSubresource().mipLevel(0);
            region.imageSubresource().baseArrayLayer(0);
            region.imageSubresource().layerCount(1);
            region.imageOffset().set(0, 0, 0);
            region.imageExtent(VkExtent3D.callocStack(stack).set(width, height, 1));

            vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);

            this.endSingleTimeCommands(commandBuffer);
        }
    }

    private void createTextureImage(String textureFilepath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            ByteBuffer pixels = stbi_load(textureFilepath, pWidth, pHeight, pChannels, STBI_rgb_alpha);

            long imageSize = pWidth.get(0) * pHeight.get(0) * 4;

            if (pixels == null) {
                throw new RuntimeException("Failed to load a texture image " + textureFilepath);
            }

            LongBuffer pStagingBuffer = stack.mallocLong(1);
            LongBuffer pStagingBufferMemory = stack.mallocLong(1);
            BufferCreator.createBuffer(
                    device,
                    imageSize,
                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pStagingBuffer,
                    pStagingBufferMemory);

            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(device, pStagingBufferMemory.get(0), 0, imageSize, 0, data);
            {
                this.memcpy(data.getByteBuffer(0, (int) imageSize), pixels, imageSize);
            }
            vkUnmapMemory(device, pStagingBufferMemory.get(0));

            stbi_image_free(pixels);

            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);
            this.createImage(
                    pWidth.get(0),
                    pHeight.get(0),
                    VK_FORMAT_R8G8B8A8_SRGB,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pTextureImage,
                    pTextureImageMemory);
            textureImage = pTextureImage.get(0);
            textureImageMemory = pTextureImageMemory.get(0);

            this.transitionImageLayout(textureImage, VK_IMAGE_LAYOUT_UNDEFINED, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);

            this.copyBufferToImage(pStagingBuffer.get(0), textureImage, pWidth.get(0), pHeight.get(0));

            this.transitionImageLayout(textureImage, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);

            vkDestroyBuffer(device, pStagingBuffer.get(0), null);
            vkFreeMemory(device, pStagingBufferMemory.get(0), null);
        }
    }

    private void createTextureImageView() {
        textureImageView = ImageViewCreator.createImageView(device, textureImage, VK_FORMAT_R8G8B8A8_SRGB);
    }

    public Texture(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            String textureFilepath) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        this.createTextureImage(textureFilepath);
        this.createTextureImageView();
    }

    public void cleanup() {
        vkDestroyImageView(device, textureImageView, null);

        vkDestroyImage(device, textureImage, null);
        vkFreeMemory(device, textureImageMemory, null);
    }
}
