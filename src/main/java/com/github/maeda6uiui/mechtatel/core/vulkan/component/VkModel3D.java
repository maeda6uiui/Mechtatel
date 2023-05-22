package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.util.ModelLoader;
import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.VkTexture;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Paths;
import java.util.*;

import static org.lwjgl.vulkan.VK10.*;

/**
 * 3D model
 *
 * @author maeda6uiui
 */
public class VkModel3D extends VkComponent3D {
    private VkDevice device;

    private boolean isDuplicatedModel;

    private ModelLoader.Model model;

    private Map<Integer, VkTexture> textures;
    private Map<Integer, Boolean> externalTextureFlags;
    private Map<Integer, Long> vertexBuffers;
    private Map<Integer, Long> vertexBufferMemories;
    private Map<Integer, Long> indexBuffers;
    private Map<Integer, Long> indexBufferMemories;

    public ModelLoader.Model getModel() {
        return model;
    }

    private void loadTextures(
            long commandPool,
            VkQueue graphicsQueue,
            List<Long> descriptorSets,
            int setCount,
            String modelFilepath) {
        String modelDir = Paths.get(modelFilepath).getParent().toString();

        Map<Integer, ModelLoader.Material> materials = model.materials;

        textures = new HashMap<>();
        externalTextureFlags = new HashMap<>();
        for (var materialEntry : materials.entrySet()) {
            int index = materialEntry.getKey();
            ModelLoader.Material material = materialEntry.getValue();

            //The filepath of the texture is supposed to be a relative path from the model
            String diffuseTexFilepath = material.diffuseTexFilepath;
            diffuseTexFilepath = Paths.get(modelDir, diffuseTexFilepath).toString();

            var texture = new VkTexture(
                    device,
                    commandPool,
                    graphicsQueue,
                    descriptorSets,
                    setCount,
                    diffuseTexFilepath,
                    true);
            textures.put(index, texture);
            externalTextureFlags.put(index, false);
        }
    }

    private void createBuffers(
            long commandPool,
            VkQueue graphicsQueue) {
        int numMeshes = model.meshes.size();

        //Create buffers
        vertexBuffers = new HashMap<>();
        vertexBufferMemories = new HashMap<>();
        indexBuffers = new HashMap<>();
        indexBufferMemories = new HashMap<>();
        for (int i = 0; i < numMeshes; i++) {
            //Create a vertex buffer and a vertex buffer memory
            BufferCreator.BufferInfo bufferInfo = BufferCreator.createVertexBuffer3DUV(
                    device, commandPool, graphicsQueue, model.meshes.get(i).vertices);
            vertexBuffers.put(i, bufferInfo.buffer);
            vertexBufferMemories.put(i, bufferInfo.bufferMemory);

            //Create an index buffer and an index buffer memory
            bufferInfo = BufferCreator.createIndexBuffer(device, commandPool, graphicsQueue, model.meshes.get(i).indices);
            indexBuffers.put(i, bufferInfo.buffer);
            indexBufferMemories.put(i, bufferInfo.bufferMemory);
        }
    }

    public VkModel3D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            List<Long> descriptorSets,
            int setCount,
            String modelFilepath) throws IOException {
        this.device = device;

        isDuplicatedModel = false;

        model = ModelLoader.loadModel(modelFilepath);
        this.loadTextures(
                commandPool,
                graphicsQueue,
                descriptorSets,
                setCount,
                modelFilepath);
        this.createBuffers(commandPool, graphicsQueue);

        this.setComponentType("gbuffer");
    }

    public VkModel3D(
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkModel3D srcModel) {
        this.device = device;

        isDuplicatedModel = true;

        model = srcModel.model;
        this.createBuffers(commandPool, graphicsQueue);

        textures = srcModel.textures;
        externalTextureFlags = srcModel.externalTextureFlags;

        this.setComponentType("gbuffer");
    }

    @Override
    public void cleanup() {
        //Textures
        if (!isDuplicatedModel) {
            textures.forEach((idx, texture) -> {
                if (externalTextureFlags.get(idx) == false) {
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

    public void replaceTexture(int index, VkTexture newTexture) {
        VkTexture curTexture = textures.get(index);
        curTexture.cleanup();

        textures.put(index, newTexture);
        externalTextureFlags.put(index, true);
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isVisible()) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numMeshes = model.meshes.size();
            for (int i = 0; i < numMeshes; i++) {
                VkTexture texture = textures.get(model.meshes.get(i).materialIndex);
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
                        model.meshes.get(i).indices.size(),
                        1,
                        0,
                        0,
                        0);
            }
        }
    }

    @Override
    public void transfer(VkCommandBuffer commandBuffer) {
        if (!this.isVisible()) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numMeshes = model.meshes.size();
            for (int i = 0; i < numMeshes; i++) {
                LongBuffer lVertexBuffers = stack.longs(vertexBuffers.get(i));
                LongBuffer offsets = stack.longs(0);
                vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

                vkCmdBindIndexBuffer(commandBuffer, indexBuffers.get(i), 0, VK_INDEX_TYPE_UINT32);

                vkCmdDrawIndexed(
                        commandBuffer,
                        model.meshes.get(i).indices.size(),
                        1,
                        0,
                        0,
                        0);
            }
        }
    }
}
