package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.MttVertex3D;
import com.github.maeda6uiui.mechtatel.core.util.VertexUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.LongBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Capsule
 *
 * @author maeda6uiui
 */
public class VkMttCapsule3D extends VkMttComponent3D {
    private VkDevice device;

    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;

    private int numIndices;

    private void createBuffers(
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex3D> vertices,
            List<Integer> indices) {
        BufferCreator.BufferInfo bufferInfo = BufferCreator.createVertexBuffer3D(
                device,
                commandPool,
                graphicsQueue,
                vertices);
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;

        bufferInfo = BufferCreator.createIndexBuffer(
                device,
                commandPool,
                graphicsQueue,
                indices);
        indexBuffer = bufferInfo.buffer;
        indexBufferMemory = bufferInfo.bufferMemory;
    }

    public VkMttCapsule3D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            Vector3fc center,
            float length,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        this.device = device;

        List<MttVertex3D> vertices = VertexUtils.createCapsuleVertices(center, length, radius, numVDivs, numHDivs, color);
        List<Integer> indices = VertexUtils.createCapsuleIndices(numVDivs, numHDivs);

        numIndices = indices.size();

        this.createBuffers(commandPool, graphicsQueue, vertices, indices);

        this.setComponentType("primitive");
    }

    @Override
    public void cleanup() {
        vkDestroyBuffer(device, vertexBuffer, null);
        vkFreeMemory(device, vertexBufferMemory, null);

        vkDestroyBuffer(device, indexBuffer, null);
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
                    numIndices,
                    1,
                    0,
                    0,
                    0);
        }
    }
}
