package com.github.maeda6uiui.mechtatel.core.vulkan.drawer;

import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import org.joml.Vector2f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.Arrays;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Draws a quadrangle
 *
 * @author maeda6uiui
 */
public class QuadDrawer {
    private VkDevice device;

    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;

    private void createBuffersAndMemories(long commandPool, VkQueue graphicsQueue, MttVertex2DUV[] vertices) {
        if (vertices.length != 4) {
            throw new IllegalArgumentException("Number of the vertices must be 4");
        }

        BufferUtils.BufferInfo bufferInfo = BufferUtils.createBufferFromVertices2DUV(
                device, commandPool, graphicsQueue, Arrays.asList(vertices));
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;

        var indices = new ArrayList<Integer>();
        indices.add(0);
        indices.add(1);
        indices.add(2);
        indices.add(2);
        indices.add(3);
        indices.add(0);

        bufferInfo = BufferUtils.createIndexBuffer(device, commandPool, graphicsQueue, indices);
        indexBuffer = bufferInfo.buffer;
        indexBufferMemory = bufferInfo.bufferMemory;
    }

    public QuadDrawer(VkDevice device, long commandPool, VkQueue graphicsQueue, MttVertex2DUV[] vertices) {
        this.device = device;

        this.createBuffersAndMemories(commandPool, graphicsQueue, vertices);
    }

    public QuadDrawer(VkDevice device, long commandPool, VkQueue graphicsQueue) {
        this.device = device;

        var vertices = new MttVertex2DUV[4];

        vertices[0] = new MttVertex2DUV(
                new Vector2f(-1.0f, -1.0f),
                new Vector4f(0.0f, 0.0f, 0.0f, 0.0f),
                new Vector2f(0.0f, 0.0f));
        vertices[1] = new MttVertex2DUV(
                new Vector2f(-1.0f, 1.0f),
                new Vector4f(0.0f, 0.0f, 0.0f, 0.0f),
                new Vector2f(0.0f, 1.0f));
        vertices[2] = new MttVertex2DUV(
                new Vector2f(1.0f, 1.0f),
                new Vector4f(0.0f, 0.0f, 0.0f, 0.0f),
                new Vector2f(1.0f, 1.0f));
        vertices[3] = new MttVertex2DUV(
                new Vector2f(1.0f, -1.0f),
                new Vector4f(0.0f, 0.0f, 0.0f, 0.0f),
                new Vector2f(1.0f, 0.0f));

        this.createBuffersAndMemories(commandPool, graphicsQueue, vertices);
    }

    public void cleanup() {
        vkDestroyBuffer(device, vertexBuffer, null);
        vkFreeMemory(device, vertexBufferMemory, null);

        vkDestroyBuffer(device, indexBuffer, null);
        vkFreeMemory(device, indexBufferMemory, null);
    }

    public void draw(VkCommandBuffer commandBuffer) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer lVertexBuffers = stack.longs(vertexBuffer);
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

            vkCmdBindIndexBuffer(commandBuffer, indexBuffer, 0, VK_INDEX_TYPE_UINT32);

            vkCmdDrawIndexed(
                    commandBuffer,
                    6,
                    1,
                    0,
                    0,
                    0);
        }
    }
}
