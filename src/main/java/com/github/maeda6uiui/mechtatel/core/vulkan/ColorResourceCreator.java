package com.github.maeda6uiui.mechtatel.core.vulkan;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkQueue;

import java.nio.LongBuffer;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates color resources
 *
 * @author maeda
 */
class ColorResourceCreator {
    public static class ColorResources {
        public long colorImage;
        public long colorImageMemory;
        public long colorImageView;
    }

    public static ColorResources createColorResources(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkExtent2D swapchainExtent,
            int msaaSamples,
            int swapchainImageFormat) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pColorImage = stack.mallocLong(1);
            LongBuffer pColorImageMemory = stack.mallocLong(1);

            ImageUtils.createImage(
                    device,
                    swapchainExtent.width(),
                    swapchainExtent.height(),
                    1,
                    msaaSamples,
                    swapchainImageFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_TRANSIENT_ATTACHMENT_BIT | VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pColorImage,
                    pColorImageMemory);
            long colorImage = pColorImage.get(0);
            long colorImageMemory = pColorImageMemory.get(0);

            long colorImageView = ImageViewCreator.createImageView(
                    device, colorImage, swapchainImageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1);

            ImageUtils.transitionImageLayout(
                    device,
                    commandPool,
                    graphicsQueue,
                    colorImage,
                    false,
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    1);

            var ret = new ColorResources();
            ret.colorImage = colorImage;
            ret.colorImageMemory = colorImageMemory;
            ret.colorImageView = colorImageView;

            return ret;
        }
    }
}
