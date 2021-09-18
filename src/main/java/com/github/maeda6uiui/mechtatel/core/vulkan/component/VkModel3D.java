package com.github.maeda6uiui.mechtatel.core.vulkan.component;

import com.github.maeda6uiui.mechtatel.core.vulkan.creator.BufferCreator;
import com.github.maeda6uiui.mechtatel.core.vulkan.texture.Texture;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.ModelLoader;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

/**
 * 3D model
 *
 * @author maeda
 */
public class VkModel3D extends VkComponent3D {
    private VkDevice device;

    private ModelLoader.Model model;

    private Map<Integer, Texture> textures;
    private Map<Integer, Long> vertexBuffers;
    private Map<Integer, Long> vertexBufferMemories;
    private Map<Integer, Long> indexBuffers;
    private Map<Integer, Long> indexBufferMemories;

    private void loadTextures(
            long commandPool,
            VkQueue graphicsQueue,
            int dstBinding,
            List<Long> descriptorSets,
            String modelFilepath) {
        String modelDir = Paths.get(modelFilepath).getParent().toString();

        Map<Integer, ModelLoader.Material> materials = model.materials;

        textures = new HashMap<>();

        for (var materialEntry : materials.entrySet()) {
            int index = materialEntry.getKey();
            ModelLoader.Material material = materialEntry.getValue();

            //The filepath of the texture is supposed to be a relative path from the model
            String diffuseTexFilepath = material.diffuseTexFilepath;
            diffuseTexFilepath = Paths.get(modelDir, diffuseTexFilepath).toString();

            var texture = new Texture(
                    device,
                    commandPool,
                    graphicsQueue,
                    dstBinding,
                    descriptorSets,
                    diffuseTexFilepath,
                    true);
            textures.put(index, texture);
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
            int dstBinding,
            List<Long> descriptorSets,
            String modelFilepath) {
        this.device = device;

        model = ModelLoader.loadModel(modelFilepath);
        this.loadTextures(
                commandPool,
                graphicsQueue,
                dstBinding,
                descriptorSets,
                modelFilepath);
        this.createBuffers(commandPool, graphicsQueue);
    }

    @Override
    public void cleanup() {
        //Texture
        textures.forEach((idx, texture) -> texture.cleanup());

        //Buffers
        vertexBuffers.forEach((idx, vertexBuffer) -> vkDestroyBuffer(device, vertexBuffer, null));
        indexBuffers.forEach((idx, indexBuffer) -> vkDestroyBuffer(device, indexBuffer, null));

        //Buffer memories
        vertexBufferMemories.forEach((idx, vertexBufferMemory) -> vkFreeMemory(device, vertexBufferMemory, null));
        indexBufferMemories.forEach((idx, indexBufferMemory) -> vkFreeMemory(device, indexBufferMemory, null));
    }

    @Override
    public void draw(
            VkCommandBuffer commandBuffer,
            int commandBufferIndex,
            long pipelineLayout,
            long textureSampler,
            int dstBinding) {
        if (!this.getVisible()) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numMeshes = model.meshes.size();
            for (int i = 0; i < numMeshes; i++) {
                Texture texture = textures.get(model.meshes.get(i).materialIndex);
                if (texture == null) {
                    continue;
                }

                ByteBuffer textureIndexBuffer = stack.calloc(1 * Integer.BYTES);
                textureIndexBuffer.putInt(texture.getTextureIndex());
                textureIndexBuffer.rewind();

                vkCmdPushConstants(
                        commandBuffer,
                        pipelineLayout,
                        VK_SHADER_STAGE_FRAGMENT_BIT,
                        1 * 16 * Float.BYTES,
                        textureIndexBuffer);

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
