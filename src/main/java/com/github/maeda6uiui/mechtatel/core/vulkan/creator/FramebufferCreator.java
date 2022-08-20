package com.github.maeda6uiui.mechtatel.core.vulkan.creator;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates framebuffers
 *
 * @author maeda
 */
public class FramebufferCreator {
    public static List<Long> createSwapchainFramebuffers(
            VkDevice device,
            List<Long> imageViews,
            long colorImageView,
            long depthImageView,
            long renderPass,
            VkExtent2D extent) {
        var framebuffers = new ArrayList<Long>(imageViews.size());

        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer attachments = stack.longs(colorImageView, depthImageView, VK_NULL_HANDLE);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(extent.width());
            framebufferInfo.height(extent.height());
            framebufferInfo.layers(1);

            for (long imageView : imageViews) {
                attachments.put(2, imageView);
                framebufferInfo.pAttachments(attachments);

                if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create a framebuffer");
                }

                framebuffers.add(pFramebuffer.get(0));
            }

            return framebuffers;
        }
    }

    public static long createFramebuffer(
            VkDevice device,
            long colorImageView,
            long depthImageView,
            long renderPass,
            VkExtent2D extent) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer attachments = stack.longs(colorImageView, depthImageView);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(extent.width());
            framebufferInfo.height(extent.height());
            framebufferInfo.layers(1);
            framebufferInfo.pAttachments(attachments);

            if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a framebuffer");
            }

            return pFramebuffer.get(0);
        }
    }
}
