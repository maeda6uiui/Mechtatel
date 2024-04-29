package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttPrimitiveVertex;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Line
 *
 * @author maeda6uiui
 */
public class VkMttLine extends VkMttComponent {
    private VkDevice device;

    private long vertexBuffer;
    private long vertexBufferMemory;

    private void createBuffer(long commandPool, VkQueue graphicsQueue, List<MttPrimitiveVertex> vertices) {
        BufferUtils.BufferInfo bufferInfo = BufferUtils.createPrimitiveVerticesBufferFromStackMemory(
                device,
                commandPool,
                graphicsQueue,
                vertices);
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;
    }

    public VkMttLine(
            IMttComponentForVkMttComponent mttComponent,
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen,
            MttPrimitiveVertex v1,
            MttPrimitiveVertex v2) {
        super(mttComponent, screen, "primitive");

        this.device = device;

        var vertices = new ArrayList<MttPrimitiveVertex>();
        vertices.add(v1);
        vertices.add(v2);

        this.createBuffer(commandPool, graphicsQueue, vertices);
    }

    @Override
    public void cleanup() {
        vkDestroyBuffer(device, vertexBuffer, null);
        vkFreeMemory(device, vertexBufferMemory, null);
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isValid() || !this.isVisible()) {
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
