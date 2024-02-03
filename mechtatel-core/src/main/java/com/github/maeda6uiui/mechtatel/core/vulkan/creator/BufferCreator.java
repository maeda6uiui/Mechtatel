package com.github.maeda6uiui.mechtatel.core.vulkan.creator;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex2D;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertexUV;
import com.github.maeda6uiui.mechtatel.core.util.MemcpyUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.MemoryUtils;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates buffers
 *
 * @author maeda6uiui
 */
public class BufferCreator {
    public static class BufferInfo {
        public long buffer;
        public long bufferMemory;
    }

    public static void createBuffer(VkDevice device, long size, int usage, int properties, LongBuffer pBuffer, LongBuffer pBufferMemory) {
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

    private static void copyBuffer(VkDevice device, long commandPool, VkQueue graphicsQueue, long srcBuffer, long dstBuffer, long size) {
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

    public static BufferInfo createVertexBuffer2D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex2D> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long bufferSize = MttVertex2D.SIZEOF * vertices.size();

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

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, data);
            {
                MemcpyUtils.memcpyVertex2D(data.getByteBuffer(0, (int) bufferSize), vertices);
            }
            vkUnmapMemory(device, stagingBufferMemory);

            createBuffer(
                    device,
                    bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory);
            long vertexBuffer = pBuffer.get(0);
            long vertexBufferMemory = pBufferMemory.get(0);

            copyBuffer(device, commandPool, graphicsQueue, stagingBuffer, vertexBuffer, bufferSize);

            vkDestroyBuffer(device, stagingBuffer, null);
            vkFreeMemory(device, stagingBufferMemory, null);

            var ret = new BufferInfo();
            ret.buffer = vertexBuffer;
            ret.bufferMemory = vertexBufferMemory;

            return ret;
        }
    }

    public static BufferInfo createVertexBuffer2DUV(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex2DUV> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long bufferSize = MttVertex2DUV.SIZEOF * vertices.size();

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

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, data);
            {
                MemcpyUtils.memcpyVertex2DUV(data.getByteBuffer(0, (int) bufferSize), vertices);
            }
            vkUnmapMemory(device, stagingBufferMemory);

            createBuffer(
                    device,
                    bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory);
            long vertexBuffer = pBuffer.get(0);
            long vertexBufferMemory = pBufferMemory.get(0);

            copyBuffer(device, commandPool, graphicsQueue, stagingBuffer, vertexBuffer, bufferSize);

            vkDestroyBuffer(device, stagingBuffer, null);
            vkFreeMemory(device, stagingBufferMemory, null);

            var ret = new BufferInfo();
            ret.buffer = vertexBuffer;
            ret.bufferMemory = vertexBufferMemory;

            return ret;
        }
    }

    public static BufferInfo createVertexBuffer3D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long bufferSize = MttVertex.SIZEOF * vertices.size();

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

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, data);
            {
                MemcpyUtils.memcpyVertex(data.getByteBuffer(0, (int) bufferSize), vertices);
            }
            vkUnmapMemory(device, stagingBufferMemory);

            createBuffer(
                    device,
                    bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory);
            long vertexBuffer = pBuffer.get(0);
            long vertexBufferMemory = pBufferMemory.get(0);

            copyBuffer(device, commandPool, graphicsQueue, stagingBuffer, vertexBuffer, bufferSize);

            vkDestroyBuffer(device, stagingBuffer, null);
            vkFreeMemory(device, stagingBufferMemory, null);

            var ret = new BufferInfo();
            ret.buffer = vertexBuffer;
            ret.bufferMemory = vertexBufferMemory;

            return ret;
        }
    }

    public static BufferInfo createVertexBuffer3DUV(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertexUV> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long bufferSize = MttVertexUV.SIZEOF * vertices.size();

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

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, data);
            {
                MemcpyUtils.memcpyVertexUV(data.getByteBuffer(0, (int) bufferSize), vertices);
            }
            vkUnmapMemory(device, stagingBufferMemory);

            createBuffer(
                    device,
                    bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_VERTEX_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory);
            long vertexBuffer = pBuffer.get(0);
            long vertexBufferMemory = pBufferMemory.get(0);

            copyBuffer(device, commandPool, graphicsQueue, stagingBuffer, vertexBuffer, bufferSize);

            vkDestroyBuffer(device, stagingBuffer, null);
            vkFreeMemory(device, stagingBufferMemory, null);

            var ret = new BufferInfo();
            ret.buffer = vertexBuffer;
            ret.bufferMemory = vertexBufferMemory;

            return ret;
        }
    }

    public static void updateVertexBuffer3DUV(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            long vertexBuffer,
            List<MttVertexUV> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long bufferSize = MttVertexUV.SIZEOF * vertices.size();

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

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, data);
            {
                MemcpyUtils.memcpyVertexUV(data.getByteBuffer(0, (int) bufferSize), vertices);
            }
            vkUnmapMemory(device, stagingBufferMemory);

            copyBuffer(device, commandPool, graphicsQueue, stagingBuffer, vertexBuffer, bufferSize);

            vkDestroyBuffer(device, stagingBuffer, null);
            vkFreeMemory(device, stagingBufferMemory, null);
        }
    }

    public static BufferInfo createIndexBuffer(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<Integer> indices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long bufferSize = Integer.BYTES * indices.size();

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

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, data);
            {
                MemcpyUtils.memcpyIntegers(data.getByteBuffer(0, (int) bufferSize), indices);
            }
            vkUnmapMemory(device, stagingBufferMemory);

            createBuffer(
                    device,
                    bufferSize,
                    VK_BUFFER_USAGE_TRANSFER_DST_BIT | VK_BUFFER_USAGE_INDEX_BUFFER_BIT,
                    VK_MEMORY_PROPERTY_DEVICE_LOCAL_BIT,
                    pBuffer,
                    pBufferMemory);
            long indexBuffer = pBuffer.get(0);
            long indexBufferMemory = pBufferMemory.get(0);

            copyBuffer(device, commandPool, graphicsQueue, stagingBuffer, indexBuffer, bufferSize);

            vkDestroyBuffer(device, stagingBuffer, null);
            vkFreeMemory(device, stagingBufferMemory, null);

            var ret = new BufferInfo();
            ret.buffer = indexBuffer;
            ret.bufferMemory = indexBufferMemory;

            return ret;
        }
    }

    public static void updateIndexBuffer(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            long indexBuffer,
            List<Integer> indices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            long bufferSize = Integer.BYTES * indices.size();

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

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(device, stagingBufferMemory, 0, bufferSize, 0, data);
            {
                MemcpyUtils.memcpyIntegers(data.getByteBuffer(0, (int) bufferSize), indices);
            }
            vkUnmapMemory(device, stagingBufferMemory);

            copyBuffer(device, commandPool, graphicsQueue, stagingBuffer, indexBuffer, bufferSize);

            vkDestroyBuffer(device, stagingBuffer, null);
            vkFreeMemory(device, stagingBufferMemory, null);
        }
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
