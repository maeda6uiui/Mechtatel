package com.github.maeda6uiui.mechtatel.core.vulkan.swapchain;

import com.github.maeda6uiui.mechtatel.core.vulkan.util.ImageUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.QueueFamilyUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.SwapchainUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.github.maeda6uiui.mechtatel.core.vulkan.util.DepthResourceUtils.findDepthFormat;
import static com.github.maeda6uiui.mechtatel.core.vulkan.util.DepthResourceUtils.hasStencilComponent;
import static com.github.maeda6uiui.mechtatel.core.vulkan.util.SwapchainUtils.querySwapchainSupport;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Swapchain
 *
 * @author maeda
 */
public class Swapchain {
    private static final int UINT32_MAX = 0xFFFFFFFF;

    private VkDevice device;

    private long swapchain;
    private VkExtent2D swapchainExtent;
    private int swapchainImageFormat;
    private List<Long> swapchainImages;
    private List<Long> swapchainImageViews;

    private long colorImage;
    private long colorImageMemory;
    private long colorImageView;
    private long depthImage;
    private long depthImageMemory;
    private long depthImageView;
    private long renderPass;
    private List<Long> swapchainFramebuffers;

    private VkSurfaceFormatKHR chooseSwapSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
        return availableFormats.stream()
                .filter(availableFormat -> availableFormat.format() == VK_FORMAT_B8G8R8_SRGB)
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

        VkExtent2D actualExtent = VkExtent2D.mallocStack().set(width, height);

        VkExtent2D minExtent = capabilities.minImageExtent();
        VkExtent2D maxExtent = capabilities.maxImageExtent();

        actualExtent.width(this.clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
        actualExtent.height(this.clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));

        return actualExtent;
    }

    private void createSwapchain(
            long surface,
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

            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.callocStack(stack);
            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            createInfo.surface(surface);
            createInfo.minImageCount(imageCount.get(0));
            createInfo.imageFormat(swapchainImageFormat);
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(swapchainExtent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            QueueFamilyUtils.QueueFamilyIndices indices
                    = QueueFamilyUtils.findQueueFamilies(device.getPhysicalDevice(), surface);
            if (!indices.graphicsFamily.equals(indices.presentFamily)) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(indices.graphicsFamily, indices.presentFamily));
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

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
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

    private void createImages(
            long commandPool,
            VkQueue graphicsQueue,
            int msaaSamples) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            //Color image
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
                    VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pColorImage,
                    pColorImageMemory);
            colorImage = pColorImage.get(0);
            colorImageMemory = pColorImageMemory.get(0);

            ImageUtils.transitionImageLayout(
                    device,
                    commandPool,
                    graphicsQueue,
                    colorImage,
                    false,
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL,
                    1);

            VkImageViewCreateInfo viewInfo = VkImageViewCreateInfo.callocStack(stack);
            viewInfo.sType(VK_STRUCTURE_TYPE_IMAGE_VIEW_CREATE_INFO);
            viewInfo.viewType(VK_IMAGE_VIEW_TYPE_2D);
            viewInfo.image(colorImage);
            viewInfo.format(swapchainImageFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_COLOR_BIT);
            viewInfo.subresourceRange().baseMipLevel(0);
            viewInfo.subresourceRange().levelCount(1);
            viewInfo.subresourceRange().baseArrayLayer(0);
            viewInfo.subresourceRange().layerCount(1);

            LongBuffer pImageView = stack.mallocLong(1);
            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            colorImageView = pImageView.get(0);

            //Depth image
            LongBuffer pDepthImage = stack.mallocLong(1);
            LongBuffer pDepthImageMemory = stack.mallocLong(1);

            int depthFormat = findDepthFormat(device);

            ImageUtils.createImage(
                    device,
                    swapchainExtent.width(),
                    swapchainExtent.height(),
                    1,
                    msaaSamples,
                    depthFormat,
                    VK_IMAGE_TILING_OPTIMAL,
                    VK_IMAGE_USAGE_DEPTH_STENCIL_ATTACHMENT_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pDepthImage,
                    pDepthImageMemory);
            depthImage = pDepthImage.get(0);
            depthImageMemory = pDepthImageMemory.get(0);

            ImageUtils.transitionImageLayout(
                    device,
                    commandPool,
                    graphicsQueue,
                    depthImage,
                    hasStencilComponent(depthFormat),
                    VK_IMAGE_LAYOUT_UNDEFINED,
                    VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL, 1);

            viewInfo.image(depthImage);
            viewInfo.format(depthFormat);
            viewInfo.subresourceRange().aspectMask(VK_IMAGE_ASPECT_DEPTH_BIT);

            if (vkCreateImageView(device, viewInfo, null, pImageView) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create an image view");
            }
            depthImageView = pImageView.get(0);
        }
    }

    private void createRenderPass(int msaaSamples) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkAttachmentDescription.Buffer attachments = VkAttachmentDescription.callocStack(3, stack);
            VkAttachmentReference.Buffer attachmentRefs = VkAttachmentReference.callocStack(3, stack);

            //Color attachments
            VkAttachmentDescription colorAttachment = attachments.get(0);
            colorAttachment.format(swapchainImageFormat);
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

            //Depth-stencil attachments
            VkAttachmentDescription depthAttachment = attachments.get(1);
            depthAttachment.format(findDepthFormat(device));
            depthAttachment.samples(msaaSamples);
            depthAttachment.loadOp(VK_ATTACHMENT_LOAD_OP_CLEAR);
            depthAttachment.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            depthAttachment.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            depthAttachment.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            depthAttachment.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            depthAttachment.finalLayout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            VkAttachmentReference depthAttachmentRef = attachmentRefs.get(1);
            depthAttachmentRef.attachment(1);
            depthAttachmentRef.layout(VK_IMAGE_LAYOUT_DEPTH_STENCIL_ATTACHMENT_OPTIMAL);

            //Present image
            VkAttachmentDescription colorAttachmentResolve = attachments.get(2);
            colorAttachmentResolve.format(swapchainImageFormat);
            colorAttachmentResolve.samples(VK_SAMPLE_COUNT_1_BIT);
            colorAttachmentResolve.loadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachmentResolve.storeOp(VK_ATTACHMENT_STORE_OP_STORE);
            colorAttachmentResolve.stencilLoadOp(VK_ATTACHMENT_LOAD_OP_DONT_CARE);
            colorAttachmentResolve.stencilStoreOp(VK_ATTACHMENT_STORE_OP_DONT_CARE);
            colorAttachmentResolve.initialLayout(VK_IMAGE_LAYOUT_UNDEFINED);
            colorAttachmentResolve.finalLayout(VK_IMAGE_LAYOUT_PRESENT_SRC_KHR);

            VkAttachmentReference colorAttachmentResolveRef = attachmentRefs.get(2);
            colorAttachmentResolveRef.attachment(2);
            colorAttachmentResolveRef.layout(VK_IMAGE_LAYOUT_COLOR_ATTACHMENT_OPTIMAL);

            VkSubpassDescription.Buffer subpass = VkSubpassDescription.callocStack(1, stack);
            subpass.pipelineBindPoint(VK_PIPELINE_BIND_POINT_GRAPHICS);
            subpass.colorAttachmentCount(1);
            subpass.pColorAttachments(VkAttachmentReference.callocStack(1, stack).put(0, colorAttachmentRef));
            subpass.pDepthStencilAttachment(depthAttachmentRef);
            subpass.pResolveAttachments(VkAttachmentReference.callocStack(1, stack).put(0, colorAttachmentResolveRef));

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

            renderPass = pRenderPass.get(0);
        }
    }

    private void createFramebuffers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            swapchainFramebuffers = new ArrayList<>(swapchainImageViews.size());

            LongBuffer attachments = stack.longs(colorImageView, depthImageView, VK_NULL_HANDLE);
            LongBuffer pFramebuffer = stack.mallocLong(1);

            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack);
            framebufferInfo.sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO);
            framebufferInfo.renderPass(renderPass);
            framebufferInfo.width(swapchainExtent.width());
            framebufferInfo.height(swapchainExtent.height());
            framebufferInfo.layers(1);

            for (long swapchainImageView : swapchainImageViews) {
                attachments.put(2, swapchainImageView);
                framebufferInfo.pAttachments(attachments);

                if (vkCreateFramebuffer(device, framebufferInfo, null, pFramebuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create a framebuffer");
                }

                swapchainFramebuffers.add(pFramebuffer.get(0));
            }
        }
    }

    public Swapchain(
            VkDevice device,
            long surface,
            long commandPool,
            VkQueue graphicsQueue,
            int width,
            int height,
            int msaaSamples) {
        this.device = device;

        this.createSwapchain(surface, width, height);
        this.createImages(commandPool, graphicsQueue, msaaSamples);
        this.createRenderPass(msaaSamples);
        this.createFramebuffers();
    }

    public void cleanup() {
        vkDestroySwapchainKHR(device, swapchain, null);
        swapchainImageViews.forEach(imageView -> vkDestroyImageView(device, imageView, null));

        vkDestroyImageView(device, colorImageView, null);
        vkDestroyImage(device, colorImage, null);
        vkFreeMemory(device, colorImageMemory, null);

        vkDestroyImageView(device, depthImageView, null);
        vkDestroyImage(device, depthImage, null);
        vkFreeMemory(device, depthImageMemory, null);

        vkDestroyRenderPass(device, renderPass, null);

        swapchainFramebuffers.forEach(framebuffer -> vkDestroyFramebuffer(device, framebuffer, null));
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

    public long getRenderPass() {
        return renderPass;
    }

    public long getSwapchainFramebuffer(int index) {
        return swapchainFramebuffers.get(index);
    }
}
