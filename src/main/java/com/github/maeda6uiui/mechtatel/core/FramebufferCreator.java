package com.github.maeda6uiui.mechtatel.core;

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
class FramebufferCreator {
    public static List<Long> createFramebuffers(
            VkDevice device,
            List<Long> swapchainImageViews,
            long colorImageView,
            long depthImageView,
            long renderPass,
            VkExtent2D swapchainExtent) {
        var swapchainFramebuffers = new ArrayList<Long>(swapchainImageViews.size());

        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer attachments = stack.longs(colorImageView, depthImageView, VK_NULL_HANDLE);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(swapchainExtent.width());
            framebufferInfo.height(swapchainExtent.height());
            framebufferInfo.layers(1);

            for (long imageView : swapchainImageViews) {
                attachments.put(2, imageView);
                framebufferInfo.pAttachments(attachments);

                if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create a framebuffer");
                }

                swapchainFramebuffers.add(pFramebuffer.get(0));
            }

            return swapchainFramebuffers;
        }
    }
}
