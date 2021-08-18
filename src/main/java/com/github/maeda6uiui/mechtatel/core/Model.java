package com.github.maeda6uiui.mechtatel.core;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.*;

import java.nio.LongBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

/**
 * 3D model
 *
 * @author maeda
 */
class Model {
    private VkDevice device;
    private long commandPool;
    private long renderPass;
    private VkExtent2D swapchainExtent;
    private List<Long> swapchainFramebuffers;
    private long graphicsPipeline;
    private VkQueue graphicsQueue;
    private long textureSampler;
    private long pipelineLayout;
    private List<Long> descriptorSets;

    private String modelFilepath;

    private ModelLoader.Model model;

    private Map<Integer, Texture> textures;
    private Map<Integer, Long> vertexBuffers;
    private Map<Integer, Long> vertexBufferMemories;
    private Map<Integer, Long> indexBuffers;
    private Map<Integer, Long> indexBufferMemories;

    private void loadTextures() {
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
                    textureSampler,
                    descriptorSets,
                    diffuseTexFilepath,
                    true);
            textures.put(index, texture);
        }
    }

    private void createBuffers() {
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

    public Model(
            VkDevice device,
            long commandPool,
            long renderPass,
            VkExtent2D swapchainExtent,
            List<Long> swapchainFramebuffers,
            long graphicsPipeline,
            VkQueue graphicsQueue,
            long textureSampler,
            long pipelineLayout,
            List<Long> descriptorSets,
            String modelFilepath) {
        this.device = device;
        this.commandPool = commandPool;
        this.renderPass = renderPass;
        this.swapchainExtent = swapchainExtent;
        this.swapchainFramebuffers = swapchainFramebuffers;
        this.graphicsPipeline = graphicsPipeline;
        this.graphicsQueue = graphicsQueue;
        this.textureSampler = textureSampler;
        this.pipelineLayout = pipelineLayout;
        this.descriptorSets = descriptorSets;

        this.modelFilepath = modelFilepath;

        model = ModelLoader.loadModel(modelFilepath);
        this.loadTextures();
        this.createBuffers();
    }

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

    public List<VkCommandBuffer> draw() {
        final int commandBuffersCount = swapchainFramebuffers.size();
        var commandBuffers = new ArrayList<VkCommandBuffer>(commandBuffersCount);

        try (MemoryStack stack = MemoryStack.stackPush()) {
            VkCommandBufferAllocateInfo allocInfo = VkCommandBufferAllocateInfo.callocStack(stack);
            allocInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_ALLOCATE_INFO);
            allocInfo.level(VK_COMMAND_BUFFER_LEVEL_PRIMARY);
            allocInfo.commandPool(commandPool);
            allocInfo.commandBufferCount(commandBuffersCount);

            PointerBuffer pCommandBuffers = stack.mallocPointer(commandBuffersCount);
            if (vkAllocateCommandBuffers(device, allocInfo, pCommandBuffers) != VK_SUCCESS) {
                throw new RuntimeException("Failed to allocate command buffers");
            }

            for (int i = 0; i < commandBuffersCount; i++) {
                commandBuffers.add(new VkCommandBuffer(pCommandBuffers.get(i), device));
            }

            VkCommandBufferBeginInfo beginInfo = VkCommandBufferBeginInfo.callocStack(stack);
            beginInfo.sType(VK_STRUCTURE_TYPE_COMMAND_BUFFER_BEGIN_INFO);

            VkRenderPassBeginInfo renderPassInfo = VkRenderPassBeginInfo.callocStack(stack);
            renderPassInfo.sType(VK_STRUCTURE_TYPE_RENDER_PASS_BEGIN_INFO);
            renderPassInfo.renderPass(renderPass);
            VkRect2D renderArea = VkRect2D.callocStack(stack);
            renderArea.offset(VkOffset2D.callocStack(stack).set(0, 0));
            renderArea.extent(swapchainExtent);
            renderPassInfo.renderArea(renderArea);
            VkClearValue.Buffer clearValues = VkClearValue.callocStack(2, stack);
            clearValues.get(0).color().float32(stack.floats(0.0f, 0.0f, 0.0f, 1.0f));
            clearValues.get(1).depthStencil().set(1.0f, 0);
            renderPassInfo.pClearValues(clearValues);

            for (int i = 0; i < commandBuffersCount; i++) {
                VkCommandBuffer commandBuffer = commandBuffers.get(i);
                if (vkBeginCommandBuffer(commandBuffer, beginInfo) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to begin recording a command buffer");
                }

                renderPassInfo.framebuffer(swapchainFramebuffers.get(i));

                vkCmdBeginRenderPass(commandBuffer, renderPassInfo, VK_SUBPASS_CONTENTS_INLINE);
                {
                    vkCmdBindPipeline(commandBuffer, VK_PIPELINE_BIND_POINT_GRAPHICS, graphicsPipeline);

                    int numMeshes = model.meshes.size();
                    for (int j = 0; j < numMeshes; j++) {
                        Texture texture = textures.get(model.meshes.get(j).materialIndex);
                        //texture.updateDescriptorSets();

                        LongBuffer lVertexBuffers = stack.longs(vertexBuffers.get(j));
                        LongBuffer offsets = stack.longs(0);
                        vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

                        vkCmdBindIndexBuffer(commandBuffer, indexBuffers.get(j), 0, VK_INDEX_TYPE_UINT32);

                        vkCmdBindDescriptorSets(
                                commandBuffer,
                                VK_PIPELINE_BIND_POINT_GRAPHICS,
                                pipelineLayout,
                                0,
                                stack.longs(descriptorSets.get(i)),
                                null);

                        vkCmdDrawIndexed(
                                commandBuffer,
                                model.meshes.get(j).indices.size(),
                                1,
                                0,
                                0,
                                0);
                    }
                }
                vkCmdEndRenderPass(commandBuffer);

                if (vkEndCommandBuffer(commandBuffer) != VK_SUCCESS) {
                    throw new RuntimeException("Failed to record a command buffer");
                }
            }

            return commandBuffers;
        }
    }
}
