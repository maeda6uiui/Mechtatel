package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.MttVertex3DUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.IVkMttScreenForVkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkMttTexture;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * 2D textured quadrangle
 *
 * @author maeda6uiui
 */
public class VkMttTexturedQuad2D extends VkMttComponent {
    private VkDevice device;

    private boolean isExternalTexture;

    private VkMttTexture texture;
    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;

    private void createBuffers(
            long commandPool,
            VkQueue graphicsQueue,
            List<MttVertex3DUV> vertices) {
        if (vertices.size() != 4) {
            throw new RuntimeException("Number of vertices must be 4");
        }

        BufferCreator.BufferInfo bufferInfo = BufferCreator.createVertexBuffer3DUV(
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

    public VkMttTexturedQuad2D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            IVkMttScreenForVkMttTexture screen,
            String textureFilepath,
            boolean generateMipmaps,
            List<MttVertex3DUV> vertices) {
        this.device = device;

        isExternalTexture = false;

        texture = new VkMttTexture(
                device,
                commandPool,
                graphicsQueue,
                screen,
                textureFilepath,
                generateMipmaps);
        this.createBuffers(commandPool, graphicsQueue, vertices);

        this.setComponentType("gbuffer");
        this.setScreenName(getScreenName());
    }

    public VkMttTexturedQuad2D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttTexturedQuad2D srcQuad,
            List<MttVertex3DUV> vertices) {
        this.device = device;

        isExternalTexture = true;

        texture = srcQuad.texture;
        this.createBuffers(commandPool, graphicsQueue, vertices);

        this.setComponentType("gbuffer");
        this.setScreenName(srcQuad.getScreenName());
    }

    public VkMttTexturedQuad2D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            String screenName,
            VkMttTexture texture,
            List<MttVertex3DUV> vertices) {
        this.device = device;

        isExternalTexture = true;

        this.texture = texture;
        this.createBuffers(commandPool, graphicsQueue, vertices);

        this.setComponentType("gbuffer");
        this.setScreenName(screenName);
    }

    public void replaceTexture(VkMttTexture newTexture) {
        if (!isExternalTexture) {
            texture.cleanup();
        }

        texture = newTexture;
        isExternalTexture = true;
    }

    @Override
    public void cleanup() {
        if (!isExternalTexture) {
            texture.cleanup();
        }

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
        if (texture == null) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            ByteBuffer textureAllocationIndexBuffer = stack.calloc(1 * Integer.BYTES);
            textureAllocationIndexBuffer.putInt(texture.getAllocationIndex());
            textureAllocationIndexBuffer.rewind();

            vkCmdPushConstants(
                    commandBuffer,
                    pipelineLayout,
                    VK_SHADER_STAGE_FRAGMENT_BIT,
                    1 * 16 * Float.BYTES + 1 * Integer.BYTES,
                    textureAllocationIndexBuffer);

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

    @Override
    public void transfer(VkCommandBuffer commandBuffer) {
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
