package com.github.maeda6uiui.mechtatel.core.vulkan.util;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex2D;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertexUV;
import com.github.maeda6uiui.mechtatel.core.util.ByteBufferUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Utility methods relating to buffers
 *
 * @author maeda6uiui
 */
public class BufferUtils {
    public static class BufferInfo {
        public long buffer;
        public long bufferMemory;
    }

    public static void createBuffer(
            VkDevice device, long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.calloc(stack);
            bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferInfo.size(size);
            bufferInfo.usage(usage);
            bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            if (vkCreateBuffer(device, bufferInfo, null, pBuffer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a buffer");
            }

            VkMemoryRequirements memRequirements = VkMemoryRequirements.malloc(stack);
            vkGetBufferMemoryRequirements(device, pBuffer.get(0), memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(MemoryUtils.findMemoryType(device, memRequirements.memoryTypeBits(), properties));

            if (vkAllocateMemory(device, allocInfo, null, pBufferMemory) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate a buffer memory");
            }

            vkBindBufferMemory(device, pBuffer.get(0), pBufferMemory.get(0), 0);
        }
    }

    private static void copyBuffer(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            long srcBuffer,
            long dstBuffer,
            long size) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.calloc(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(commandPool);
            allocInfo.commandBufferCount(1);

            PointerBuffer pCommandBuffer = stack.mallocPointer(1);
            vkAllocateCommandBuffers(device, allocInfo, pCommandBuffer);
            VkCommandBuffer commandBuffer = new VkCommandBuffer(pCommandBuffer.get(0), device);

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.calloc(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);
            beginInfo.flags(VK_COMMAND_BUFFER_USAGE_ONE_TIME_SUBMIT_BIT);

            vkBeginCommandBuffer(commandBuffer, beginInfo);
            {
                VkBufferCopy.Buffer copyRegion = VkBufferCopy.calloc(1, stack);
                copyRegion.size(size);
                vkCmdCopyBuffer(commandBuffer, srcBuffer, dstBuffer, copyRegion);
            }
            vkEndCommandBuffer(commandBuffer);

            VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.pCommandBuffers(pCommandBuffer);

            if (vkQueueSubmit(graphicsQueue, submitInfo, VK_NULL_HANDLE) != VK_SUCCESS) {
                throw new RuntimeException("Failed to submit a copy command buffer");
            }

            vkQueueWaitIdle(graphicsQueue);

            vkFreeCommandBuffers(device, commandPool, pCommandBuffer);
        }
    }

    private static BufferInfo createStagingBuffer(VkDevice device, int bufferSize) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);
            createBuffer(
                    device,
                    bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_SRC_BIT,
                    VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                    pBuffer,
                    pBufferMemory);
            long stagingBuffer = pBuffer.get(0);
            long stagingBufferMemory = pBufferMemory.get(0);

            var ret = new BufferInfo();
            ret.buffer = stagingBuffer;
            ret.bufferMemory = stagingBufferMemory;

            return ret;
        }
    }

    public static BufferInfo createBufferFromByteBuffer(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            ByteBuffer dataBuffer,
            int usage) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            //Staging buffer
            int bufferSize = dataBuffer.capacity();
            BufferInfo stagingBufferInfo = createStagingBuffer(device, bufferSize);

            PointerBuffer pb = stack.mallocPointer(1);
            vkMapMemory(device, stagingBufferInfo.bufferMemory, 0, bufferSize, 0, pb);
            {
                ByteBuffer buffer = pb.getByteBuffer(0, bufferSize);
                buffer.put(dataBuffer);
                buffer.flip();
            }
            vkUnmapMemory(device, stagingBufferInfo.bufferMemory);

            //Destination buffer
            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);

            createBuffer(
                    device,
                    bufferSize,
                    usage,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory
            );
            copyBuffer(
                    device,
                    commandPool,
                    graphicsQueue,
                    stagingBufferInfo.buffer,
                    pBuffer.get(0),
                    bufferSize
            );

            //Destroy staging buffer
            vkDestroyBuffer(device, stagingBufferInfo.buffer, null);
            vkFreeMemory(device, stagingBufferInfo.bufferMemory, null);

            var ret = new BufferInfo();
            ret.buffer = pBuffer.get(0);
            ret.bufferMemory = pBufferMemory.get(0);

            return ret;
        }
    }

    public static BufferInfo createVertices2DBufferFromStackMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex2D> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer dataBuffer = ByteBufferUtils.vertices2DToByteBuffer(vertices, stack);
            return createBufferFromByteBuffer(
                    device,
                    commandPool,
                    graphicsQueue,
                    dataBuffer,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
            );
        }
    }

    public static BufferInfo createVertices2DBufferFromHeapMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex2D> vertices) {
        ByteBuffer dataBuffer = ByteBufferUtils.vertices2DToByteBuffer(vertices, null);
        BufferInfo bufferInfo = createBufferFromByteBuffer(
                device,
                commandPool,
                graphicsQueue,
                dataBuffer,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
        );
        MemoryUtil.memFree(dataBuffer);

        return bufferInfo;
    }

    public static BufferInfo createVertices2DUVBufferFromStackMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex2DUV> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer dataBuffer = ByteBufferUtils.vertices2DUVToByteBuffer(vertices, stack);
            return createBufferFromByteBuffer(
                    device,
                    commandPool,
                    graphicsQueue,
                    dataBuffer,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
            );
        }
    }

    public static BufferInfo createVertices2DUVBufferFromHeapMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex2DUV> vertices) {
        ByteBuffer dataBuffer = ByteBufferUtils.vertices2DUVToByteBuffer(vertices, null);
        BufferInfo bufferInfo = createBufferFromByteBuffer(
                device,
                commandPool,
                graphicsQueue,
                dataBuffer,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
        );
        MemoryUtil.memFree(dataBuffer);

        return bufferInfo;
    }

    public static BufferInfo createVerticesBufferFromStackMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer dataBuffer = ByteBufferUtils.verticesToByteBuffer(vertices, stack);
            return createBufferFromByteBuffer(
                    device,
                    commandPool,
                    graphicsQueue,
                    dataBuffer,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
            );
        }
    }

    public static BufferInfo createVerticesBufferFromHeapMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex> vertices) {
        ByteBuffer dataBuffer = ByteBufferUtils.verticesToByteBuffer(vertices, null);
        BufferInfo bufferInfo = createBufferFromByteBuffer(
                device,
                commandPool,
                graphicsQueue,
                dataBuffer,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
        );
        MemoryUtil.memFree(dataBuffer);

        return bufferInfo;
    }

    public static BufferInfo createVerticesUVBufferFromStackMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertexUV> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer dataBuffer = ByteBufferUtils.verticesUVToByteBuffer(vertices, stack);
            return createBufferFromByteBuffer(
                    device,
                    commandPool,
                    graphicsQueue,
                    dataBuffer,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
            );
        }
    }

    public static BufferInfo createVerticesUVBufferFromHeapMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertexUV> vertices) {
        ByteBuffer dataBuffer = ByteBufferUtils.verticesUVToByteBuffer(vertices, null);
        BufferInfo bufferInfo = createBufferFromByteBuffer(
                device,
                commandPool,
                graphicsQueue,
                dataBuffer,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT
        );
        MemoryUtil.memFree(dataBuffer);

        return bufferInfo;
    }

    public static BufferInfo createIndexBufferFromStackMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<Integer> indices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer dataBuffer = ByteBufferUtils.indicesToByteBuffer(indices, stack);
            return createBufferFromByteBuffer(
                    device,
                    commandPool,
                    graphicsQueue,
                    dataBuffer,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT
            );
        }
    }

    public static BufferInfo createIndexBufferFromHeapMemory(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<Integer> indices) {
        ByteBuffer dataBuffer = ByteBufferUtils.indicesToByteBuffer(indices, null);
        BufferInfo bufferInfo = createBufferFromByteBuffer(
                device,
                commandPool,
                graphicsQueue,
                dataBuffer,
                VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT
        );
        MemoryUtil.memFree(dataBuffer);

        return bufferInfo;
    }

    public static List<BufferInfo> createUBOBuffers(VkDevice device, int numBuffers, long size) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            var ret = new ArrayList<BufferInfo>(numBuffers);

            LongBuffer pBuffer = stack.mallocLong(1);
            LongBuffer pBufferMemory = stack.mallocLong(1);

            for (int i = 0; i < numBuffers; i++) {
                createBuffer(
                        device,
                        size,
                        VK_BUFFER_USAGE_UNIFORM_BUFFER_BIT,
                        VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT,
                        pBuffer,
                        pBufferMemory);

                var bufferInfo = new BufferInfo();
                bufferInfo.buffer = pBuffer.get(0);
                bufferInfo.bufferMemory = pBufferMemory.get(0);

                ret.add(bufferInfo);
            }

            return ret;
        }
    }
}
