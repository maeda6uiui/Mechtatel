package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.model.MttMaterial;
import com.github.maeda6uiui.mechtatel.core.model.MttModelData;
import com.github.maeda6uiui.mechtatel.core.screen.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.*;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Model
 *
 * @author maeda6uiui
 */
public class VkMttModel extends VkMttComponent {
    private VkDevice device;

    private boolean isDuplicatedModel;

    private MttModelData modelData;

    private Map<Integer, VkMttTexture> textures;
    private Map<Integer, Boolean> externalTextureFlags;
    private Map<Integer, Long> vertexBuffers;
    private Map<Integer, Long> vertexBufferMemories;
    private Map<Integer, Long> indexBuffers;
    private Map<Integer, Long> indexBufferMemories;

    private List<Integer> drawMeshIndices;

    public MttModelData getModelData() {
        return modelData;
    }

    private void loadTextures(
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen) {
        Map<Integer, MttMaterial> materials = modelData.materials;

        textures = new HashMap<>();
        externalTextureFlags = new HashMap<>();
        for (var materialEntry : materials.entrySet()) {
            int index = materialEntry.getKey();
            MttMaterial material = materialEntry.getValue();

            var texture = new VkMttTexture(
                    device,
                    commandPool,
                    graphicsQueue,
                    screen,
                    material.diffuseTexResource,
                    true);

            textures.put(index, texture);
            externalTextureFlags.put(index, false);
        }
    }

    private void createBuffers(
            long commandPool,
            VkQueue graphicsQueue) {
        int numMeshes = modelData.meshes.size();

        //Create buffers
        vertexBuffers = new HashMap<>();
        vertexBufferMemories = new HashMap<>();
        indexBuffers = new HashMap<>();
        indexBufferMemories = new HashMap<>();
        for (int i = 0; i < numMeshes; i++) {
            //Create a vertex buffer and a vertex buffer memory
            BufferUtils.BufferInfo bufferInfo = BufferUtils.createVerticesUVBufferFromHeapMemory(
                    device, commandPool, graphicsQueue, modelData.meshes.get(i).vertices);
            vertexBuffers.put(i, bufferInfo.buffer);
            vertexBufferMemories.put(i, bufferInfo.bufferMemory);

            //Create an index buffer and an index buffer memory
            bufferInfo = BufferUtils.createIndexBufferFromHeapMemory(device, commandPool, graphicsQueue, modelData.meshes.get(i).indices);
            indexBuffers.put(i, bufferInfo.buffer);
            indexBufferMemories.put(i, bufferInfo.bufferMemory);
        }
    }

    public VkMttModel(
            IMttComponentForVkMttComponent mttComponent,
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen,
            MttModelData modelData) {
        super(mttComponent, screen, "gbuffer");

        this.device = device;

        isDuplicatedModel = false;

        this.modelData = modelData;
        this.loadTextures(commandPool, graphicsQueue, screen);
        this.createBuffers(commandPool, graphicsQueue);

        drawMeshIndices = new ArrayList<>();
        for (int i = 0; i < modelData.meshes.size(); i++) {
            drawMeshIndices.add(i);
        }
    }

    public VkMttModel(
            IMttComponentForVkMttComponent mttComponent,
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttModel srcModel) {
        super(mttComponent, srcModel.getScreen(), "gbuffer");

        this.device = device;

        isDuplicatedModel = true;

        modelData = srcModel.modelData;
        this.createBuffers(commandPool, graphicsQueue);

        drawMeshIndices = new ArrayList<>();
        for (int i = 0; i < modelData.meshes.size(); i++) {
            drawMeshIndices.add(i);
        }

        textures = srcModel.textures;
        externalTextureFlags = srcModel.externalTextureFlags;
    }

    @Override
    public void cleanup() {
        //Textures
        if (!isDuplicatedModel) {
            textures.forEach((idx, texture) -> {
                if (!externalTextureFlags.get(idx)) {
                    texture.cleanup();
                }
            });
        }

        //Buffers
        vertexBuffers.forEach((idx, vertexBuffer) -> vkDestroyBuffer(device, vertexBuffer, null));
        indexBuffers.forEach((idx, indexBuffer) -> vkDestroyBuffer(device, indexBuffer, null));

        //Buffer memories
        vertexBufferMemories.forEach((idx, vertexBufferMemory) -> vkFreeMemory(device, vertexBufferMemory, null));
        indexBufferMemories.forEach((idx, indexBufferMemory) -> vkFreeMemory(device, indexBufferMemory, null));
    }

    public Set<Integer> getTextureIndices() {
        return textures.keySet();
    }

    public void replaceTexture(int index, VkMttTexture newTexture) {
        VkMttTexture curTexture = textures.get(index);
        curTexture.cleanup();

        textures.put(index, newTexture);
        externalTextureFlags.put(index, true);
    }

    public int getNumMeshes() {
        return modelData.meshes.size();
    }

    public void setDrawMeshIndices(List<Integer> drawMeshIndices) {
        this.drawMeshIndices = drawMeshIndices;
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isValid() || !this.isVisible()) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numMeshes = modelData.meshes.size();
            for (int i = 0; i < numMeshes; i++) {
                if (!drawMeshIndices.contains(i)) {
                    continue;
                }

                VkMttTexture texture = textures.get(modelData.meshes.get(i).materialIndex);
                if (texture == null) {
                    continue;
                }

                ByteBuffer textureAllocationIndexBuffer = stack.calloc(1 * Integer.BYTES);
                textureAllocationIndexBuffer.putInt(texture.getAllocationIndex());
                textureAllocationIndexBuffer.rewind();

                vkCmdPushConstants(
                        commandBuffer,
                        pipelineLayout,
                        VK_SHADER_STAGE_FRAGMENT_BIT,
                        1 * 16 * Float.BYTES + 1 * Integer.BYTES,
                        textureAllocationIndexBuffer);

                LongBuffer lVertexBuffers = stack.longs(vertexBuffers.get(i));
                LongBuffer offsets = stack.longs(0);
                vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

                vkCmdBindIndexBuffer(commandBuffer, indexBuffers.get(i), 0, VK_INDEX_TYPE_UINT32);

                vkCmdDrawIndexed(
                        commandBuffer,
                        modelData.meshes.get(i).indices.size(),
                        1,
                        0,
                        0,
                        0);
            }
        }
    }

    @Override
    public void transfer(VkCommandBuffer commandBuffer) {
        if (!this.isValid() || !this.isVisible()) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numMeshes = modelData.meshes.size();
            for (int i = 0; i < numMeshes; i++) {
                if (!drawMeshIndices.contains(i)) {
                    continue;
                }

                LongBuffer lVertexBuffers = stack.longs(vertexBuffers.get(i));
                LongBuffer offsets = stack.longs(0);
                vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

                vkCmdBindIndexBuffer(commandBuffer, indexBuffers.get(i), 0, VK_INDEX_TYPE_UINT32);

                vkCmdDrawIndexed(
                        commandBuffer,
                        modelData.meshes.get(i).indices.size(),
                        1,
                        0,
                        0,
                        0);
            }
        }
    }
}
