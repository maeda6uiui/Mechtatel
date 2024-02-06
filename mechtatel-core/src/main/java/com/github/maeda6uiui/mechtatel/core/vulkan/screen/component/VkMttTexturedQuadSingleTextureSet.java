package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex2DUV;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertexUV;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Set of textured quadrangles with a single texture
 *
 * @author maeda6uiui
 */
public class VkMttTexturedQuadSingleTextureSet extends VkMttComponent {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private List<MttVertexUV> vertices;
    private VkMttTexture texture;

    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;
    private boolean bufferCreated;
    private boolean isExternalTexture;

    public VkMttTexturedQuadSingleTextureSet(
            IMttComponentForVkMttComponent mttComponent,
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen,
            URI textureResource) {
        super(mttComponent, screen, "gbuffer");

        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        vertices = new ArrayList<>();

        texture = new VkMttTexture(
                device,
                commandPool,
                graphicsQueue,
                screen,
                textureResource,
                false);
        bufferCreated = false;
        isExternalTexture = false;
    }

    public VkMttTexturedQuadSingleTextureSet(
            IMttComponentForVkMttComponent mttComponent,
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttTexture texture) {
        super(mttComponent, texture.getScreenForVkComponent(), "gbuffer");

        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        vertices = new ArrayList<>();

        this.texture = texture;

        bufferCreated = false;
        isExternalTexture = true;
    }

    public void add(MttVertexUV v1, MttVertexUV v2, MttVertexUV v3, MttVertexUV v4) {
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);
    }

    public void add(MttVertex2DUV topLeft, MttVertex2DUV bottomRight, float z) {
        var v1 = new MttVertexUV(
                new Vector3f(topLeft.pos.x(), topLeft.pos.y(), z), topLeft.color, topLeft.texCoords);
        var v2 = new MttVertexUV(
                new Vector3f(topLeft.pos.x(), bottomRight.pos.y(), z), topLeft.color, new Vector2f(topLeft.texCoords.x(), bottomRight.texCoords.y()));
        var v3 = new MttVertexUV(
                new Vector3f(bottomRight.pos.x(), bottomRight.pos.y(), z), topLeft.color, bottomRight.texCoords);
        var v4 = new MttVertexUV(
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
        BufferUtils.BufferInfo bufferInfo = BufferUtils.createBufferFromVerticesUV(
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

        bufferInfo = BufferUtils.createIndexBuffer(
                device,
                commandPool,
                graphicsQueue,
                indices);
        indexBuffer = bufferInfo.buffer;
        indexBufferMemory = bufferInfo.bufferMemory;

        bufferCreated = true;
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
        if (bufferCreated) {
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
        if (!bufferCreated) {
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
