package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.component.Vertex2DUV;
import com.github.maeda6uiui.mechtatel.core.component.Vertex3DUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkTexture;
import org.joml.Vector2f;
import org.joml.Vector3f;
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
 * Set of 2D textured quadrangles with a single texture
 *
 * @author maeda6uiui
 */
public class VkTexturedQuad2DSingleTextureSet extends VkComponent {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private List<Vertex3DUV> vertices;
    private VkTexture texture;

    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;
    private boolean bufferCreated;
    private boolean isExternalTexture;

    public VkTexturedQuad2DSingleTextureSet(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<Long> descriptorSets,
            int setCount,
            String textureFilepath) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        vertices = new ArrayList<>();

        texture = new VkTexture(
                device,
                commandPool,
                graphicsQueue,
                descriptorSets,
                setCount,
                textureFilepath,
                false);
        bufferCreated = false;
        isExternalTexture = false;

        this.setComponentType("gbuffer");
    }

    public VkTexturedQuad2DSingleTextureSet(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkTexture texture) {
        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        vertices = new ArrayList<>();

        this.texture = texture;

        bufferCreated = false;
        isExternalTexture = true;

        this.setComponentType("gbuffer");
    }

    public void add(Vertex3DUV v1, Vertex3DUV v2, Vertex3DUV v3, Vertex3DUV v4) {
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);
    }

    public void add(Vertex2DUV topLeft, Vertex2DUV bottomRight, float z) {
        var v1 = new Vertex3DUV(
                new Vector3f(topLeft.pos.x(), topLeft.pos.y(), z), topLeft.color, topLeft.texCoords);
        var v2 = new Vertex3DUV(
                new Vector3f(topLeft.pos.x(), bottomRight.pos.y(), z), topLeft.color, new Vector2f(topLeft.texCoords.x(), bottomRight.texCoords.y()));
        var v3 = new Vertex3DUV(
                new Vector3f(bottomRight.pos.x(), bottomRight.pos.y(), z), topLeft.color, bottomRight.texCoords);
        var v4 = new Vertex3DUV(
                new Vector3f(bottomRight.pos.x(), topLeft.pos.y(), z), topLeft.color, new Vector2f(bottomRight.texCoords.x(), topLeft.texCoords.y()));

        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);
    }

    public void clear() {
        vertices.clear();
    }

    public void createBuffers() {
        BufferCreator.BufferInfo bufferInfo = BufferCreator.createVertexBuffer3DUV(
                device,
                commandPool,
                graphicsQueue,
                vertices);
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;

        var indices = new ArrayList<Integer>();
        for (int i = 0; i < vertices.size(); i += 4) {
            indices.add(i);
            indices.add(i + 1);
            indices.add(i + 2);

            indices.add(i + 2);
            indices.add(i + 3);
            indices.add(i);
        }

        bufferInfo = BufferCreator.createIndexBuffer(
                device,
                commandPool,
                graphicsQueue,
                indices);
        indexBuffer = bufferInfo.buffer;
        indexBufferMemory = bufferInfo.bufferMemory;

        bufferCreated = true;
    }

    @Override
    public void cleanup() {
        if (bufferCreated) {
            if (!isExternalTexture) {
                texture.cleanup();
            }

            vkDestroyBuffer(device, vertexBuffer, null);
            vkDestroyBuffer(device, indexBuffer, null);

            vkFreeMemory(device, vertexBufferMemory, null);
            vkFreeMemory(device, indexBufferMemory, null);

            bufferCreated = false;
        }
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isVisible()) {
            return;
        }
        if (texture == null) {
            return;
        }
        if (!bufferCreated) {
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
                    1 * 16 * Float.BYTES + 1 * Integer.BYTES,
                    textureIndexBuffer);

            LongBuffer lVertexBuffers = stack.longs(vertexBuffer);
            LongBuffer offsets = stack.longs(0);
            vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

            vkCmdBindIndexBuffer(commandBuffer, indexBuffer, 0, VK_INDEX_TYPE_UINT32);

            vkCmdDrawIndexed(
                    commandBuffer,
                    vertices.size() / 2 * 3,
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
                    vertices.size() / 2 * 3,
                    1,
                    0,
                    0,
                    0);
        }
    }
}
