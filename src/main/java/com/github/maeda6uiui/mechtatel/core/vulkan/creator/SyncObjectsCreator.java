package com.github.maeda6uiui.mechtatel.core.vulkan.creator;

import com.github.maeda6uiui.mechtatel.core.vulkan.frame.Frame;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates sync objects
 *
 * @author maeda
 */
public class SyncObjectsCreator {
    public static List<Frame> createSyncObjects(VkDevice device, int maxFramesInFlight) {
        var inFlightFrames = new ArrayList<Frame>(maxFramesInFlight);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);

            for (int i = 0; i < maxFramesInFlight; i++) {
                if (vkCreateSemaphore(device, semaphoreInfo, null, pImageAvailableSemaphore) != VK_SUCCESS
                        || vkCreateSemaphore(device, semaphoreInfo, null, pRenderFinishedSemaphore) != VK_SUCCESS
                        || vkCreateFence(device, fenceInfo, null, pFence) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to create synchronization objects for the frame " + i);
                }

                inFlightFrames.add(
                        new Frame(
                                device,
                                pImageAvailableSemaphore.get(0),
                                pRenderFinishedSemaphore.get(0),
                                pFence.get(0)));
            }

            return inFlightFrames;
        }
    }


}
