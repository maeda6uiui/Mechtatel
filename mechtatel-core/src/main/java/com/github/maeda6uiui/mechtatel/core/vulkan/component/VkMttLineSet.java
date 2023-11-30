package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.MttVertex;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import org.joml.Vector3fc;
import org.joml.Vector4fc;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Set of lines
 *
 * @author maeda6uiui
 */
public class VkMttLineSet extends VkMttComponent {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private List<MttVertex> vertices;

    private long vertexBuffer;
    private long vertexBufferMemory;
    private boolean bufferCreated;

    public VkMttLineSet(VkDevice device, long commandPool, VkQueue graphicsQueue) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        vertices = new ArrayList<>();

        bufferCreated = false;

        this.setComponentType("primitive");
    }

    public void add(MttVertex v1, MttVertex v2) {
        vertices.add(v1);
        vertices.add(v2);
    }

    public void add(Vector3fc p1, Vector4fc color1, Vector3fc p2, Vector4fc color2) {
        var v1 = new MttVertex(p1, color1);
        var v2 = new MttVertex(p2, color2);
        vertices.add(v1);
        vertices.add(v2);
    }

    public void add(Vector3fc p1, Vector3fc p2, Vector4fc color) {
        var v1 = new MttVertex(p1, color);
        var v2 = new MttVertex(p2, color);
        vertices.add(v1);
        vertices.add(v2);
    }

    public void clear(boolean doCleanup) {
        vertices.clear();
        if (doCleanup) {
            this.cleanup();
        }
    }

    public void createBuffer() {
        BufferCreator.BufferInfo bufferInfo = BufferCreator.createVertexBuffer3D(
                device,
                commandPool,
                graphicsQueue,
                vertices);
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;

        bufferCreated = true;
    }

    @Override
    public void cleanup() {
        if (bufferCreated) {
            vkDestroyBuffer(device, vertexBuffer, null);
            vkFreeMemory(device, vertexBufferMemory, null);

            bufferCreated = false;
        }
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isVisible()) {
            return;
        }
        if (!bufferCreated) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer lVertexBuffers = stack.longs(vertexBuffer);
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

            vkCmdDraw(
                    commandBuffer,
                    vertices.size(),
                    1,
                    0,
                    0);
        }
    }
}
