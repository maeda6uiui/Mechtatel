package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.MttVertex3D;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * 3D filled quadrangle
 *
 * @author maeda6uiui
 */
public class VkMttFilledQuad3D extends VkMttComponent3D {
    private VkDevice device;

    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;

    private void createBuffers(
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex3D> vertices) {
        if (vertices.size() != 4) {
            throw new RuntimeException("Number of vertices must be 4");
        }

        BufferCreator.BufferInfo bufferInfo = BufferCreator.createVertexBuffer3D(
                device, commandPool, graphicsQueue, vertices);
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;

        var indices = new ArrayList<Integer>();
        indices.add(0);
        indices.add(1);
        indices.add(2);
        indices.add(2);
        indices.add(3);
        indices.add(0);

        bufferInfo = BufferCreator.createIndexBuffer(device, commandPool, graphicsQueue, indices);
        indexBuffer = bufferInfo.buffer;
        indexBufferMemory = bufferInfo.bufferMemory;
    }

    public VkMttFilledQuad3D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex3D> vertices) {
        this.device = device;

        this.createBuffers(commandPool, graphicsQueue, vertices);
        this.setComponentType("primitive_fill");
    }

    @Override
    public void cleanup() {
        vkDestroyBuffer(device, vertexBuffer, null);
        vkDestroyBuffer(device, indexBuffer, null);

        vkFreeMemory(device, vertexBufferMemory, null);
        vkFreeMemory(device, indexBufferMemory, null);
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isVisible()) {
            return;
        }

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
