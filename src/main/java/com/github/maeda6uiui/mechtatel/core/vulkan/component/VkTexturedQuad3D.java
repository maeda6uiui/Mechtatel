package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.Vertex3DUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.Texture;
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
 * 3D textured quadrangle
 *
 * @author maeda
 */
public class VkTexturedQuad3D extends VkComponent3D {
    private VkDevice device;

    private boolean isExternalTexture;

    private Texture texture;
    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;

    private void createBuffers(
            long commandPool,
            VkQueue graphicsQueue,
            List<Vertex3DUV> vertices) {
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

    public VkTexturedQuad3D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<Long> descriptorSets,
            int setCount,
            String textureFilepath,
            boolean generateMipmaps,
            List<Vertex3DUV> vertices) {
        this.device = device;

        isExternalTexture = false;

        texture = new Texture(
                device,
                commandPool,
                graphicsQueue,
                descriptorSets,
                setCount,
                textureFilepath,
                generateMipmaps);
        this.createBuffers(commandPool, graphicsQueue, vertices);

        this.setComponentType("gbuffer");
    }

    public VkTexturedQuad3D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkTexturedQuad3D srcQuad,
            List<Vertex3DUV> vertices) {
        this.device = device;

        isExternalTexture = true;

        texture = srcQuad.texture;
        this.createBuffers(commandPool, graphicsQueue, vertices);

        this.setComponentType("gbuffer");
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
            ByteBuffer textureIndexBuffer = stack.calloc(1 * Integer.BYTES);
            textureIndexBuffer.putInt(texture.getTextureIndex());
            textureIndexBuffer.rewind();

            vkCmdPushConstants(
                    commandBuffer,
                    pipelineLayout,
                    VK_SHADER_STAGE_FRAGMENT_BIT,
                    1 * 16 * Float.BYTES,
                    textureIndexBuffer);

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
