package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.Vertex3D;
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
 * 2D line
 *
 * @author maeda
 */
public class VkLine2D extends VkComponent {
    private VkDevice device;

    private long vertexBuffer;
    private long vertexBufferMemory;

    private void createBuffer(long commandPool, VkQueue graphicsQueue, List<Vertex3D> vertices) {
        BufferCreator.BufferInfo bufferInfo = BufferCreator.createVertexBuffer3D(
                device,
                commandPool,
                graphicsQueue,
                vertices);
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;
    }

    public VkLine2D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            Vertex3D v1,
            Vertex3D v2) {
        this.device = device;

        var vertices = new ArrayList<Vertex3D>();
        vertices.add(v1);
        vertices.add(v2);

        this.createBuffer(commandPool, graphicsQueue, vertices);

        this.setComponentType("primitive");
    }

    @Override
    public void cleanup() {
        vkDestroyBuffer(device, vertexBuffer, null);
        vkFreeMemory(device, vertexBufferMemory, null);
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

            vkCmdDraw(
                    commandBuffer,
                    2,
                    1,
                    0,
                    0);
        }
    }
}
