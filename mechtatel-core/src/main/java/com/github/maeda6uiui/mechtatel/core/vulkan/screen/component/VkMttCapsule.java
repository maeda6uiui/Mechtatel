package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex;
import com.github.maeda6uiui.mechtatel.core.util.VertexUtils;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
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
public class VkMttCapsule extends VkMttComponent {
    private VkDevice device;

    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;

    private int numIndices;

    private void createBuffers(
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex> vertices,
            List<Integer> indices) {
        BufferUtils.BufferInfo bufferInfo = BufferUtils.createVertexBuffer3D(
                device,
                commandPool,
                graphicsQueue,
                vertices);
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;

        bufferInfo = BufferUtils.createIndexBuffer(
                device,
                commandPool,
                graphicsQueue,
                indices);
        indexBuffer = bufferInfo.buffer;
        indexBufferMemory = bufferInfo.bufferMemory;
    }

    public VkMttCapsule(
            IMttComponentForVkMttComponent mttComponent,
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen,
            Vector3fc center,
            float length,
            float radius,
            int numVDivs,
            int numHDivs,
            Vector4fc color) {
        super(mttComponent, screen, "primitive");

        this.device = device;

        List<MttVertex> vertices = VertexUtils.createCapsuleVertices(center, length, radius, numVDivs, numHDivs, color);
        List<Integer> indices = VertexUtils.createCapsuleIndices(numVDivs, numHDivs);

        numIndices = indices.size();

        this.createBuffers(commandPool, graphicsQueue, vertices, indices);
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
