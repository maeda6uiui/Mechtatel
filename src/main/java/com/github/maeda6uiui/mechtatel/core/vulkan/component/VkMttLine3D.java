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
 * 3D line
 *
 * @author maeda6uiui
 */
public class VkMttLine3D extends VkMttComponent {
    private VkDevice device;

    private long vertexBuffer;
    private long vertexBufferMemory;

    private void createBuffer(long commandPool, VkQueue graphicsQueue, List<MttVertex3D> vertices) {
        BufferCreator.BufferInfo bufferInfo = BufferCreator.createVertexBuffer3D(
                device,
                commandPool,
                graphicsQueue,
                vertices);
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;
    }

    public VkMttLine3D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            MttVertex3D v1,
            MttVertex3D v2) {
        this.device = device;

        var vertices = new ArrayList<MttVertex3D>();
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
