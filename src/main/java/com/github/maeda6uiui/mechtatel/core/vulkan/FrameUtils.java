package com.github.maeda6uiui.mechtatel.core.vulkan;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetTime;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Utility methods for frames
 *
 * @author maeda
 */
class FrameUtils {
    private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

    private static void memcpyCameraUBO(ByteBuffer buffer, CameraUBO ubo) {
        final int mat4size = 16 * Float.BYTES;

        ubo.model.get(0, buffer);
        ubo.view.get(AlignmentUtils.alignas(mat4size, AlignmentUtils.alignof(ubo.view)), buffer);
        ubo.proj.get(AlignmentUtils.alignas(mat4size * 2, AlignmentUtils.alignof(ubo.proj)), buffer);

        buffer.rewind();
    }

    private static void updateUniformBuffer(
            VkDevice device,
            VkExtent2D swapchainExtent,
            List<Long> uniformBufferMemories,
            int currentImage) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var ubo = new CameraUBO();

            ubo.model.rotate((float) (glfwGetTime() * Math.toRadians(20)), 0.0f, 1.0f, 0.0f);
            ubo.view.lookAt(5.0f, 5.0f, 5.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
            ubo.proj.perspective(
                    (float) Math.toRadians(45),
                    (float) swapchainExtent.width() / (float) swapchainExtent.height(),
                    0.1f,
                    500.0f);
            ubo.proj.m11(ubo.proj.m11() * (-1.0f));

            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(device, uniformBufferMemories.get(currentImage), 0, CameraUBO.SIZEOF, 0, data);
            {
                memcpyCameraUBO(data.getByteBuffer(0, CameraUBO.SIZEOF), ubo);
            }
            vkUnmapMemory(device, uniformBufferMemories.get(currentImage));
        }
    }

    public static void drawFrame(
            VkDevice device,
            Frame thisFrame,
            long swapchain,
            VkExtent2D swapchainExtent,
            Map<Integer, Frame> imagesInFlight,
            List<VkCommandBuffer> commandBuffers,
            VkQueue graphicsQueue,
            VkQueue presentQueue,
            List<Long> uniformBufferMemories) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            vkWaitForFences(device, thisFrame.pFence(), true, UINT64_MAX);

            IntBuffer pImageIndex = stack.mallocInt(1);
            vkAcquireNextImageKHR(device, swapchain, UINT64_MAX, thisFrame.imageAvailableSemaphore(), VK_NULL_HANDLE, pImageIndex);
            final int imageIndex = pImageIndex.get(0);

            updateUniformBuffer(device, swapchainExtent, uniformBufferMemories, imageIndex);

            if (imagesInFlight.containsKey(imageIndex)) {
                vkWaitForFences(device, imagesInFlight.get(imageIndex).fence(), true, UINT64_MAX);
            }

            imagesInFlight.put(imageIndex, thisFrame);

            VkSubmitInfo submitInfo = VkSubmitInfo.callocStack(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.waitSemaphoreCount(1);
            submitInfo.pWaitSemaphores(thisFrame.pImageAvailableSemaphore());
            submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
            submitInfo.pSignalSemaphores(thisFrame.pRenderFinishedSemaphore());
            submitInfo.pCommandBuffers(stack.pointers(commandBuffers.get(imageIndex)));

            vkResetFences(device, thisFrame.pFence());

            if (vkQueueSubmit(graphicsQueue, submitInfo, thisFrame.fence()) != VK_SUCCESS) {
                throw new RuntimeException("Failed to submit a draw command buffer");
            }

            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.callocStack(stack);
            presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
            presentInfo.pWaitSemaphores(thisFrame.pRenderFinishedSemaphore());
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(swapchain));
            presentInfo.pImageIndices(pImageIndex);

            vkQueuePresentKHR(presentQueue, presentInfo);
        }
    }
}
