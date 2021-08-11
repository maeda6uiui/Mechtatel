package com.github.maeda6uiui.mechtatel.core;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Creates vertex buffers
 *
 * @author maeda
 */
class VertexBufferCreator {
    public static class VertexBufferInfo {
        public long vertexBuffer;
        public long vertexBufferMemory;
    }

    private static void memcpy(ByteBuffer buffer, List<Vertex2D> vertices) {
        for (var vertex : vertices) {
            buffer.putFloat(vertex.pos.x());
            buffer.putFloat(vertex.pos.y());

            buffer.putFloat(vertex.color.x());
            buffer.putFloat(vertex.color.y());
            buffer.putFloat(vertex.color.z());
        }
    }

    private static int findMemoryType(VkDevice device, int typeFilter, int properties) {
        VkPhysicalDeviceMemoryProperties memProperties = VkPhysicalDeviceMemoryProperties.mallocStack();
        vkGetPhysicalDeviceMemoryProperties(device.getPhysicalDevice(), memProperties);

        for (int i = 0; i < memProperties.memoryTypeCount(); i++) {
            if ((typeFilter & (1 << i)) != 0 && (memProperties.memoryTypes(i).propertyFlags() & properties) == properties) {
                return i;
            }
        }

        throw new RuntimeException("Failed to find a suitable memory type");
    }

    public static VertexBufferInfo createVertexBuffer2D(VkDevice device, List<Vertex2D> vertices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkBufferCreateInfo bufferInfo = VkBufferCreateInfo.callocStack(stack);
            bufferInfo.sType(VK_STRUCTURE_TYPE_BUFFER_CREATE_INFO);
            bufferInfo.size(Vertex2D.SIZEOF * vertices.size());
            bufferInfo.usage(VK_BUFFER_USAGE_VERTEX_BUFFER_BIT);
            bufferInfo.sharingMode(VK_SHARING_MODE_EXCLUSIVE);

            LongBuffer pVertexBuffer = stack.mallocLong(1);
            if (vkCreateBuffer(device, bufferInfo, null, pVertexBuffer) != VK_SUCCESS) {
                throw new RuntimeException("Failed to create a vertex buffer");
            }
            long vertexBuffer = pVertexBuffer.get(0);

            VkMemoryRequirements memRequirements = VkMemoryRequirements.mallocStack(stack);
            vkGetBufferMemoryRequirements(device, vertexBuffer, memRequirements);

            VkMemoryAllocateInfo allocInfo = VkMemoryAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_MEMORY_ALLOCATE_INFO);
            allocInfo.allocationSize(memRequirements.size());
            allocInfo.memoryTypeIndex(
                    findMemoryType(
                            device, memRequirements.memoryTypeBits(),
                            VK_MEMORY_PROPERTY_HOST_VISIBLE_BIT | VK_MEMORY_PROPERTY_HOST_COHERENT_BIT));

            LongBuffer pVertexBufferMemory = stack.mallocLong(1);
            if (vkAllocateMemory(device, allocInfo, null, pVertexBufferMemory) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate a vertex buffer memory");
            }
            long vertexBufferMemory = pVertexBufferMemory.get(0);

            vkBindBufferMemory(device, vertexBuffer, vertexBufferMemory, 0);

            PointerBuffer data = stack.mallocPointer(1);

            vkMapMemory(device, vertexBufferMemory, 0, bufferInfo.size(), 0, data);
            {
                memcpy(data.getByteBuffer(0, (int) bufferInfo.size()), vertices);
            }
            vkUnmapMemory(device, vertexBufferMemory);

            var ret = new VertexBufferInfo();
            ret.vertexBuffer = vertexBuffer;
            ret.vertexBufferMemory = vertexBufferMemory;

            return ret;
        }
    }
}
