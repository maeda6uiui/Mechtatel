package com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture;

import com.github.maeda6uiui.mechtatel.core.vulkan.creator.ImageViewCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.gbuffer.GBufferNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.IVkMttScreenForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.IVkMttScreenForVkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.CommandBufferUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.stb.STBImage.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Texture
 *
 * @author maeda6uiui
 */
public class VkMttTexture {
    private static final Map<Integer, Boolean> allocationStatus;
    private static int imageFormat;

    static {
        allocationStatus = new HashMap<>();
        for (int i = 0; i < GBufferNabor.MAX_NUM_TEXTURES; i++) {
            allocationStatus.put(i, false);
        }

        imageFormat = VK_FORMAT_R8G8B8A8_SRGB;
    }

    private synchronized static int allocateTextureIndex() {
        int index = -1;

        for (var entry : allocationStatus.entrySet()) {
            if (!entry.getValue()) {
                index = entry.getKey();
                allocationStatus.put(index, true);

                break;
            }
        }

        return index;
    }

    public static void setImageFormatToSRGB() {
        imageFormat = VK_FORMAT_R8G8B8A8_SRGB;
    }

    public static void setImageFormatToUNORM() {
        imageFormat = VK_FORMAT_R8G8B8A8_UNORM;
    }

    private VkDevice device;

    private IVkMttScreenForVkMttTexture screen;
    private IVkMttScreenForVkMttComponent screenForVkComponent;
    private int allocationIndex;
    private boolean externalImage;

    private long textureImage;
    private long textureImageMemory;
    private long textureImageView;

    private int width;
    private int height;
    private int mipLevels;

    private Path textureFile;
    private boolean generateMipmaps;

    private void memcpy(ByteBuffer dst, ByteBuffer src, long size) {
        src.limit((int) size);
        dst.put(src);
        src.limit(src.capacity()).rewind();
    }

    private double log2(double n) {
        return Math.log(n) / Math.log(2);
    }

    private void copyBufferToImage(
            long commandPool,
            VkQueue graphicsQueue,
            long buffer,
            long image,
            int width,
            int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            VkBufferImageCopy.Buffer region = VkBufferImageCopy.calloc(1, stack);
            region.bufferOffset(0);
            region.bufferRowLength(0);
            region.bufferImageHeight(0);
            region.imageSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            region.imageSubresource().mipLevel(0);
            region.imageSubresource().baseArrayLayer(0);
            region.imageSubresource().layerCount(1);
            region.imageOffset().set(0, 0, 0);
            region.imageExtent(VkExtent3D.calloc(stack).set(width, height, 1));

            vkCmdCopyBufferToImage(commandBuffer, buffer, image, VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL, region);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void generateMipmaps(long commandPool, VkQueue graphicsQueue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkFormatProperties formatProperties = VkFormatProperties.malloc(stack);
            vkGetPhysicalDeviceFormatProperties(device.getPhysicalDevice(), VK_FORMAT_R8G8B8A8_UNORM, formatProperties);

            if ((formatProperties.optimalTilingFeatures() & VK_FORMAT_FEATURE_SAMPLED_IMAGE_FILTER_LINEAR_BIT) == 0) {
                throw new RuntimeException("Texture image format does not support linear blitting");
            }

            VkCommandBuffer commandBuffer = CommandBufferUtils.beginSingleTimeCommands(device, commandPool);

            VkImageMemoryBarrier.Buffer barrier = VkImageMemoryBarrier.calloc(1, stack);
            barrier.sType(VK_STRUCTURE_TYPE_IMAGE_MEMORY_BARRIER);
            barrier.image(textureImage);
            barrier.srcQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.dstQueueFamilyIndex(VK_QUEUE_FAMILY_IGNORED);
            barrier.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            barrier.subresourceRange().baseArrayLayer(0);
            barrier.subresourceRange().layerCount(1);
            barrier.subresourceRange().levelCount(1);

            int mipWidth = width;
            int mipHeight = height;

            for (int i = 1; i < mipLevels; i++) {
                barrier.subresourceRange().baseMipLevel(i - 1);
                barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
                barrier.newLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
                barrier.dstAccessMask(VK_ACCESS_TRANSFER_READ_BIT);

                vkCmdPipelineBarrier(
                        commandBuffer,
                        VK_PIPELINE_STAGE_TRANSFER_BIT,
                        VK_PIPELINE_STAGE_TRANSFER_BIT,
                        0,
                        null,
                        null,
                        barrier);

                VkImageBlit.Buffer blit = VkImageBlit.calloc(1, stack);
                blit.srcOffsets(0).set(0, 0, 0);
                blit.srcOffsets(1).set(mipWidth, mipHeight, 1);
                blit.srcSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
                blit.srcSubresource().mipLevel(i - 1);
                blit.srcSubresource().baseArrayLayer(0);
                blit.srcSubresource().layerCount(1);
                blit.dstOffsets(0).set(0, 0, 0);
                blit.dstOffsets(1).set(mipWidth > 1 ? mipWidth / 2 : 1, mipHeight > 1 ? mipHeight / 2 : 1, 1);
                blit.dstSubresource().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
                blit.dstSubresource().mipLevel(i);
                blit.dstSubresource().baseArrayLayer(0);
                blit.dstSubresource().layerCount(1);

                vkCmdBlitImage(
                        commandBuffer,
                        textureImage,
                        VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL,
                        textureImage,
                        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                        blit,
                        VK_FILTER_LINEAR);

                barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_SRC_OPTIMAL);
                barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
                barrier.srcAccessMask(VK_ACCESS_TRANSFER_READ_BIT);
                barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

                vkCmdPipelineBarrier(
                        commandBuffer,
                        VK_PIPELINE_STAGE_TRANSFER_BIT,
                        VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                        0,
                        null,
                        null,
                        barrier);

                if (mipWidth > 1) {
                    mipWidth /= 2;
                }
                if (mipHeight > 1) {
                    mipHeight /= 2;
                }
            }

            barrier.subresourceRange().baseMipLevel(mipLevels - 1);
            barrier.oldLayout(VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL);
            barrier.newLayout(VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL);
            barrier.srcAccessMask(VK_ACCESS_TRANSFER_WRITE_BIT);
            barrier.dstAccessMask(VK_ACCESS_SHADER_READ_BIT);

            vkCmdPipelineBarrier(
                    commandBuffer,
                    VK_PIPELINE_STAGE_TRANSFER_BIT,
                    VK_PIPELINE_STAGE_FRAGMENT_SHADER_BIT,
                    0,
                    null,
                    null,
                    barrier);

            CommandBufferUtils.endSingleTimeCommands(device, commandPool, commandBuffer, graphicsQueue);
        }
    }

    private void createTextureImageFromByteBuffer(
            long commandPool,
            VkQueue graphicsQueue,
            ByteBuffer pixels,
            int width,
            int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long imageSize = width * height * 4;
            this.width = width;
            this.height = height;

            mipLevels = (int) Math.floor(this.log2(Math.max(width, height))) + 1;

            LongBuffer pStagingBuffer = stack.mallocLong(1);
            LongBuffer pStagingBufferMemory = stack.mallocLong(1);
            BufferUtils.createBuffer(
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

            LongBuffer pTextureImage = stack.mallocLong(1);
            LongBuffer pTextureImageMemory = stack.mallocLong(1);
            ImageUtils.createImage(
                    device,
                    width,
                    height,
                    mipLevels,
                    VK_SAMPLE_COUNT_1_BIT,
                    imageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_TRANSFER_SRC_BIT | VK_IMAGE_USAGE_TRANSFER_DST_BIT | VK_IMAGE_USAGE_SAMPLED_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pTextureImage,
                    pTextureImageMemory);
            textureImage = pTextureImage.get(0);
            textureImageMemory = pTextureImageMemory.get(0);

            ImageUtils.transitionImageLayout(
                    device,
                    commandPool,
                    graphicsQueue,
                    textureImage,
                    VK_IMAGE_ASPECT_COLOR_BIT,
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    mipLevels);

            this.copyBufferToImage(
                    commandPool,
                    graphicsQueue,
                    pStagingBuffer.get(0),
                    textureImage,
                    width,
                    height);

            if (generateMipmaps) {
                this.generateMipmaps(commandPool, graphicsQueue);
            } else {
                ImageUtils.transitionImageLayout(
                        device,
                        commandPool,
                        graphicsQueue,
                        textureImage,
                        VK_IMAGE_ASPECT_COLOR_BIT,
                        VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                        VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                        1);
            }

            vkDestroyBuffer(device, pStagingBuffer.get(0), null);
            vkFreeMemory(device, pStagingBufferMemory.get(0), null);
        }
    }

    private void createTextureImage(long commandPool, VkQueue graphicsQueue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pWidth = stack.mallocInt(1);
            IntBuffer pHeight = stack.mallocInt(1);
            IntBuffer pChannels = stack.mallocInt(1);

            ByteBuffer pixels = stbi_load(textureFile.toString(), pWidth, pHeight, pChannels, STBI_rgb_alpha);
            if (pixels == null) {
                throw new RuntimeException("Failed to load a texture image " + textureFile);
            }

            this.createTextureImageFromByteBuffer(commandPool, graphicsQueue, pixels, pWidth.get(0), pHeight.get(0));

            stbi_image_free(pixels);
        }
    }

    private void createTextureImageView() {
        textureImageView = ImageViewCreator.createImageView(
                device,
                textureImage,
                imageFormat,
                VK_IMAGE_ASPECT_COLOR_BIT,
                generateMipmaps ? mipLevels : 1);
    }

    public VkMttTexture(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen,
            Path textureFile,
            boolean generateMipmaps) {
        allocationIndex = allocateTextureIndex();
        if (allocationIndex < 0) {
            String msg = String.format("Cannot create more than %d textures", GBufferNabor.MAX_NUM_TEXTURES);
            throw new RuntimeException(msg);
        }

        this.device = device;

        this.textureFile = textureFile;
        this.generateMipmaps = generateMipmaps;

        //Create a texture image and a texture image view
        this.createTextureImage(commandPool, graphicsQueue);
        this.createTextureImageView();

        screen.updateTextureDescriptorSets(allocationIndex, textureImageView);
        this.screen = screen;
        this.screenForVkComponent = screen;

        externalImage = false;
    }

    public VkMttTexture(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen,
            ByteBuffer pixels,
            int width,
            int height,
            boolean generateMipmaps) {
        allocationIndex = allocateTextureIndex();
        if (allocationIndex < 0) {
            String msg = String.format("Cannot create more than %d textures", GBufferNabor.MAX_NUM_TEXTURES);
            throw new RuntimeException(msg);
        }

        this.device = device;

        this.generateMipmaps = generateMipmaps;

        //Create a texture image and a texture image view
        this.createTextureImageFromByteBuffer(commandPool, graphicsQueue, pixels, width, height);
        this.createTextureImageView();

        screen.updateTextureDescriptorSets(allocationIndex, textureImageView);
        this.screen = screen;
        this.screenForVkComponent = screen;

        externalImage = false;
    }

    public VkMttTexture(VkDevice device, VkMttScreen screen, long imageView) {
        allocationIndex = allocateTextureIndex();
        if (allocationIndex < 0) {
            String msg = String.format("Cannot create more than %d textures", GBufferNabor.MAX_NUM_TEXTURES);
            throw new RuntimeException(msg);
        }

        this.device = device;
        this.textureImageView = imageView;

        screen.updateTextureDescriptorSets(allocationIndex, textureImageView);
        this.screen = screen;
        this.screenForVkComponent = screen;

        externalImage = true;
    }

    public void cleanup() {
        if (!externalImage) {
            vkDestroyImage(device, textureImage, null);
            vkFreeMemory(device, textureImageMemory, null);
            vkDestroyImageView(device, textureImageView, null);
        }

        synchronized (allocationStatus) {
            allocationStatus.put(allocationIndex, false);
        }
        screen.resetTextureDescriptorSets(allocationIndex);
    }

    public int getAllocationIndex() {
        return allocationIndex;
    }

    public long getTextureImageView() {
        return textureImageView;
    }

    public IVkMttScreenForVkMttComponent getScreenForVkComponent() {
        return screenForVkComponent;
    }
}
