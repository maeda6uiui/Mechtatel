package com.github.maeda6uiui.mechtatel.core.vulkan.frame;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Frame
 *
 * @author maeda
 */
public class Frame {
    private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

    private VkDevice device;

    private final long imageAvailableSemaphore;
    private final long renderFinishedSemaphore;
    private final long fence;

    public Frame(VkDevice device, long imageAvailableSemaphore, long renderFinishedSemaphore, long fence) {
        this.device = device;

        this.imageAvailableSemaphore = imageAvailableSemaphore;
        this.renderFinishedSemaphore = renderFinishedSemaphore;
        this.fence = fence;
    }

    public long imageAvailableSemaphore() {
        return imageAvailableSemaphore;
    }

    public LongBuffer pImageAvailableSemaphore() {
        return MemoryStack.stackGet().longs(imageAvailableSemaphore);
    }

    public long renderFinishedSemaphore() {
        return renderFinishedSemaphore;
    }

    public LongBuffer pRenderFinishedSemaphore() {
        return MemoryStack.stackGet().longs(renderFinishedSemaphore);
    }

    public long fence() {
        return fence;
    }

    public LongBuffer pFence() {
        return MemoryStack.stackGet().longs(fence);
    }

    public int present(
            long swapchain,
            Map<Integer, Frame> imagesInFlight,
            List<VkCommandBuffer> commandBuffers,
            VkQueue graphicsQueue,
            VkQueue presentQueue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            vkWaitForFences(device, this.pFence(), true, UINT64_MAX);

            IntBuffer pImageIndex = stack.mallocInt(1);
            int vkResult = vkAcquireNextImageKHR(
                    device, swapchain, UINT64_MAX, this.imageAvailableSemaphore(), VK_NULL_HANDLE, pImageIndex);
            if (vkResult == VK_ERROR_OUT_OF_DATE_KHR) {
                return -1;
            }

            final int imageIndex = pImageIndex.get(0);

            if (imagesInFlight.containsKey(imageIndex)) {
                vkWaitForFences(device, imagesInFlight.get(imageIndex).fence(), true, UINT64_MAX);
            }

            imagesInFlight.put(imageIndex, this);

            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.waitSemaphoreCount(1);
            submitInfo.pWaitSemaphores(this.pImageAvailableSemaphore());
            submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
            submitInfo.pSignalSemaphores(this.pRenderFinishedSemaphore());
            submitInfo.pCommandBuffers(stack.pointers(commandBuffers.get(imageIndex)));

            vkResetFences(device, this.pFence());

            if (vkQueueSubmit(graphicsQueue, submitInfo, this.fence()) != VK_SUCCESS) {
                throw new RuntimeException("Failed to submit a draw command buffer");
            }

            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
            presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
            presentInfo.pWaitSemaphores(this.pRenderFinishedSemaphore());
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(swapchain));
            presentInfo.pImageIndices(pImageIndex);

            vkResult = vkQueuePresentKHR(presentQueue, presentInfo);
            if (vkResult == VK_ERROR_OUT_OF_DATE_KHR || vkResult == VK_SUBOPTIMAL_KHR) {
                return -1;
            } else if (vkResult != VK_SUCCESS) {
                throw new RuntimeException("Failed to present a swapchain image");
            }

            return 0;
        }
    }
}