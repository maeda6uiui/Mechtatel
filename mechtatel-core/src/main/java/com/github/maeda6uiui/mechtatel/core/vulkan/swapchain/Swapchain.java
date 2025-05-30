package com.github.maeda6uiui.mechtatel.core.vulkan.swapchain;

import com.github.maeda6uiui.mechtatel.core.vulkan.util.SwapchainUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.github.maeda6uiui.mechtatel.core.vulkan.util.SwapchainUtils.querySwapchainSupport;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Swapchain
 *
 * @author maeda6uiui
 */
public class Swapchain {
    private static final int UINT32_MAX = 0xFFFFFFFF;

    private VkDevice device;

    private long swapchain;
    private VkExtent2D swapchainExtent;
    private int swapchainImageFormat;
    private List<Long> swapchainImages;
    private List<Long> swapchainImageViews;
    private List<Long> swapchainFramebuffers;

    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
        return availableFormats.stream()
                .filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8A8_SRGB)
                .filter(availableFormat -> availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR)
                .findAny()
                .orElse(availableFormats.get(0));
    }

    private int chooseSwapPresentMode(IntBuffer availablePresentModes) {
        for (int i = 0; i < availablePresentModes.capacity(); i++) {
            if (availablePresentModes.get(i) == VK_PRESENT_MODE_MAILBOX_KHR) {
                return availablePresentModes.get(i);
            }
        }

        return VK_PRESENT_MODE_FIFO_KHR;
    }

    private int clamp(int min, int max, int value) {
        return Math.max(min, Math.min(max, value));
    }

    private VkExtent2D chooseSwapExtent(VkSurfaceCapabilitiesKHR capabilities, int width, int height) {
        if (capabilities.currentExtent().width() != UINT32_MAX) {
            return capabilities.currentExtent();
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkExtent2D actualExtent = VkExtent2D.malloc(stack).set(width, height);

            VkExtent2D minExtent = capabilities.minImageExtent();
            VkExtent2D maxExtent = capabilities.maxImageExtent();

            actualExtent.width(this.clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
            actualExtent.height(this.clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

            return actualExtent;
        }
    }

    private void createSwapchain(
            long surface,
            int graphicsFamilyIndex,
            int presentFamilyIndex,
            int width,
            int height) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SwapchainUtils.SwapchainSupportDetails swapchainSupport
                    = querySwapchainSupport(device.getPhysicalDevice(), surface, stack);

            VkSurfaceFormatKHR surfaceFormat = this.chooseSwapSurfaceFormat(swapchainSupport.formats);
            int presentMode = chooseSwapPresentMode(swapchainSupport.presentModes);
            VkExtent2D extent = chooseSwapExtent(swapchainSupport.capabilities, width, height);
            swapchainExtent = VkExtent2D.create().set(extent);

            IntBuffer imageCount = stack.ints(swapchainSupport.capabilities.minImageCount() + 1);
            if (swapchainSupport.capabilities.maxImageCount() > 0 && imageCount.get(0) > swapchainSupport.capabilities.maxImageCount()) {
                imageCount.put(0, swapchainSupport.capabilities.maxImageCount());
            }

            swapchainImageFormat = surfaceFormat.format();

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            createInfo.surface(surface);
            createInfo.minImageCount(imageCount.get(0));
            createInfo.imageFormat(swapchainImageFormat);
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(swapchainExtent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            if (graphicsFamilyIndex != presentFamilyIndex) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(graphicsFamilyIndex, presentFamilyIndex));
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            createInfo.preTransform(swapchainSupport.capabilities.currentTransform());
            createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);
            createInfo.oldSwapchain(VK_NULL_HANDLE);

            LongBuffer pSwapchain = stack.longs(VK_NULL_HANDLE);
            if (vkCreateSwapchainKHR(device, createInfo, null, pSwapchain) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a swapchain");
            }
            swapchain = pSwapchain.get(0);

            //Create swapchain images
            vkGetSwapchainImagesKHR(device, swapchain, imageCount, null);

            LongBuffer pSwapchainImages = stack.mallocLong(imageCount.get(0));
            vkGetSwapchainImagesKHR(device, swapchain, imageCount, pSwapchainImages);

            swapchainImages = new ArrayList<>(imageCount.get(0));
            for (int i = 0; i < pSwapchainImages.capacity(); i++) {
                swapchainImages.add(pSwapchainImages.get(i));
            }

            //Create swapchain image views
            swapchainImageViews = new ArrayList<>(swapchainImages.size());

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.calloc(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.format(swapchainImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(1);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            for (var swapchainImage : swapchainImages) {
                viewInfo.image(swapchainImage);

                LongBuffer pImageView = stack.mallocLong(1);
                if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create an image view");
                }

                swapchainImageViews.add(pImageView.get(0));
            }
        }
    }

    public Swapchain(
            VkDevice device,
            long surface,
            int graphicsFamilyIndex,
            int presentFamilyIndex,
            int width,
            int height) {
        this.device = device;

        this.createSwapchain(surface, graphicsFamilyIndex, presentFamilyIndex, width, height);
    }

    public void createFramebuffers(long renderPass) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            swapchainFramebuffers = new ArrayList<>(swapchainImageViews.size());

            LongBuffer attachments = stack.longs(VK_NULL_HANDLE);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.calloc(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(swapchainExtent.width());
            framebufferInfo.height(swapchainExtent.height());
            framebufferInfo.layers(1);

            for (long swapchainImageView : swapchainImageViews) {
                attachments.put(0, swapchainImageView);
                framebufferInfo.pAttachments(attachments);

                if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create a framebuffer");
                }

                swapchainFramebuffers.add(pFramebuffer.get(0));
            }
        }
    }

    public void cleanup() {
        vkDestroySwapchainKHR(device, swapchain, null);
        swapchainImageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));
        if (swapchainFramebuffers != null) {
            swapchainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));
        }
    }

    public long getSwapchain() {
        return swapchain;
    }

    public int getNumSwapchainImages() {
        return swapchainImages.size();
    }

    public int getSwapchainImageFormat() {
        return swapchainImageFormat;
    }

    public VkExtent2D getSwapchainExtent() {
        return swapchainExtent;
    }

    public long getSwapchainFramebuffer(int index) {
        return swapchainFramebuffers.get(index);
    }
}
