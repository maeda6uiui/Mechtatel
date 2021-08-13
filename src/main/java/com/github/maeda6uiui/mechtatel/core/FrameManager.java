package com.github.maeda6uiui.mechtatel.core;

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
 * Frame manager
 *
 * @author maeda
 */
class FrameManager {
    private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;

    private static void memcpyUBO(ByteBuffer buffer, UniformBufferObject ubo) {
        final int mat4size = 16 * Float.BYTES;

        ubo.model.get(0, buffer);
        ubo.view.get(mat4size, buffer);
        ubo.proj.get(mat4size * 2, buffer);

        buffer.rewind();
    }

    public static void updateUniformBuffer(
            VkDevice device,
            Frame thisFrame,
            long swapchain,
            VkExtent2D swapchainExtent,
            List<Long> uniformBufferMemories) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var ubo = new UniformBufferObject();

            ubo.model.rotate((float) (glfwGetTime() * Math.toRadians(20)), 0.0f, 0.0f, 1.0f);
            ubo.view.lookAt(2.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
            ubo.proj.perspective(
                    (float) Math.toRadians(90),
                    (float) swapchainExtent.width() / (float) swapchainExtent.height(),
                    0.1f,
                    500.0f);
            ubo.proj.m11(ubo.proj.m11() * (-1.0f));

            vkWaitForFences(device, thisFrame.pFence(), true, UINT64_MAX);

            IntBuffer pImageIndex = stack.mallocInt(1);
            vkAcquireNextImageKHR(device, swapchain, UINT64_MAX, thisFrame.imageAvailableSemaphore(), VK_NULL_HANDLE, pImageIndex);
            final int imageIndex = pImageIndex.get(0);

            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(device, uniformBufferMemories.get(imageIndex), 0, UniformBufferObject.SIZEOF, 0, data);
            {
                memcpyUBO(data.getByteBuffer(0, UniformBufferObject.SIZEOF), ubo);
            }
            vkUnmapMemory(device, uniformBufferMemories.get(imageIndex));
        }
    }

    public static void drawFrame(
            VkDevice device,
            Frame thisFrame,
            long swapchain,
            Map<Integer, Frame> imagesInFlight,
            List<VkCommandBuffer> commandBuffers,
            VkQueue graphicsQueue,
            VkQueue presentQueue) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            vkWaitForFences(device, thisFrame.pFence(), true, UINT64_MAX);

            IntBuffer pImageIndex = stack.mallocInt(1);
            vkAcquireNextImageKHR(device, swapchain, UINT64_MAX, thisFrame.imageAvailableSemaphore(), VK_NULL_HANDLE, pImageIndex);
            final int imageIndex = pImageIndex.get(0);

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
