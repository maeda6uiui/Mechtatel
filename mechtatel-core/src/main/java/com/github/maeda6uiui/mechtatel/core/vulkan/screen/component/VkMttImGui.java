package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertex;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import imgui.ImDrawData;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.vulkan.VK10.*;

/**
 * Component for ImGui rendering
 *
 * @author maeda6uiui
 */
public class VkMttImGui extends VkMttComponent {
    private VkDevice device;
    private long commandPool;
    private VkQueue graphicsQueue;

    private ImDrawData drawData;

    private Map<Integer, Long> vertexBuffers;
    private Map<Integer, Long> vertexBufferMemories;
    private Map<Integer, Long> indexBuffers;
    private Map<Integer, Long> indexBufferMemories;
    private Map<Integer, Integer> numIndicesMap;

    public VkMttImGui(
            IMttComponentForVkMttComponent mttComponent,
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen) {
        super(mttComponent, screen, "gbuffer_imgui");

        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        vertexBuffers = new HashMap<>();
        vertexBufferMemories = new HashMap<>();
        indexBuffers = new HashMap<>();
        indexBufferMemories = new HashMap<>();
        numIndicesMap = new HashMap<>();
    }

    @Override
    public void cleanup() {
        vertexBuffers.values().forEach(v -> vkDestroyBuffer(device, v, null));
        vertexBufferMemories.values().forEach(v -> vkFreeMemory(device, v, null));
        indexBuffers.values().forEach(v -> vkDestroyBuffer(device, v, null));
        indexBufferMemories.values().forEach(v -> vkFreeMemory(device, v, null));
    }

    /**
     * Sets draw data of ImGui for the next draw operation.
     * Draw data must be set via this method every time before calling {@link #draw(VkCommandBuffer, long)},
     * otherwise draw operation is not executed.
     *
     * @param drawData Draw data of ImGui
     */
    public void setDrawData(ImDrawData drawData) {
        this.drawData = drawData;
    }

    private List<MttVertex> convertImGuiVtxBufferDataToVertices(ByteBuffer vtxBufferData, float z) {
        var vertices = new ArrayList<MttVertex>();
        int numVertices = vtxBufferData.remaining() / ImDrawData.sizeOfImDrawVert();
        for (int i = 0; i < numVertices; i++) {
            float posX = vtxBufferData.getFloat();
            float posY = vtxBufferData.getFloat();
            float texU = vtxBufferData.getFloat();
            float texV = vtxBufferData.getFloat();
            int col = vtxBufferData.getInt();

            int colR = col & 0xff;
            int colG = (col >> 8) & 0xff;
            int colB = (col >> 16) & 0xff;
            int colA = (col >> 24) & 0xff;

            var vertex = new MttVertex(
                    new Vector3f(posX, posY, z),
                    new Vector4f(colR / 255.0f, colG / 255.0f, colB / 255.0f, colA / 255.0f),
                    new Vector2f(texU, texV)
            );
            vertices.add(vertex);
        }

        return vertices;
    }

    private List<Integer> convertImGuiIdxBufferDataToIntegerList(ByteBuffer idxBufferData) {
        var indices = new ArrayList<Integer>();
        int numIndices = idxBufferData.remaining() / ImDrawData.sizeOfImDrawIdx();
        for (int i = 0; i < numIndices; i++) {
            int index = idxBufferData.getShort();
            indices.add(index);
        }

        return indices;
    }

    private void recordCommands(VkCommandBuffer commandBuffer, Long pipelineLayout) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numLists = drawData.getCmdListsCount();
            for (int i = 0; i < numLists; i++) {
                //Get vertex buffer if already exists
                long vertexBuffer;
                if (vertexBuffers.containsKey(i)) {
                    vertexBuffer = vertexBuffers.get(i);
                }
                //Otherwise create new one
                else {
                    List<MttVertex> vertices = this.convertImGuiVtxBufferDataToVertices(
                            drawData.getCmdListVtxBufferData(i), 0.0f
                    );

                    BufferUtils.BufferInfo bufferInfo = BufferUtils.createVerticesBufferFromHeapMemory(
                            device,
                            commandPool,
                            graphicsQueue,
                            vertices
                    );
                    vertexBuffer = bufferInfo.buffer;

                    vertexBuffers.put(i, bufferInfo.buffer);
                    vertexBufferMemories.put(i, bufferInfo.bufferMemory);
                }

                //Get index buffer if already exists
                long indexBuffer;
                if (indexBuffers.containsKey(i)) {
                    indexBuffer = indexBuffers.get(i);
                }
                //Otherwise create new one
                else {
                    List<Integer> indices = this.convertImGuiIdxBufferDataToIntegerList(
                            drawData.getCmdListIdxBufferData(i)
                    );

                    BufferUtils.BufferInfo bufferInfo = BufferUtils.createIndexBufferFromHeapMemory(
                            device,
                            commandPool,
                            graphicsQueue,
                            indices
                    );
                    indexBuffer = bufferInfo.buffer;

                    indexBuffers.put(i, bufferInfo.buffer);
                    indexBufferMemories.put(i, bufferInfo.bufferMemory);

                    int numIndices = indices.size();
                    numIndicesMap.put(i, numIndices);
                }

                //Bind vertex buffer
                LongBuffer lVertexBuffers = stack.longs(vertexBuffer);
                LongBuffer offsets = stack.longs(0);
                vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

                //Bind index buffer
                vkCmdBindIndexBuffer(commandBuffer, indexBuffer, 0, VK_INDEX_TYPE_UINT32);

                //Record command
                if (pipelineLayout != null) {
                    int numCmds = drawData.getCmdListCmdBufferSize(i);
                    for (int j = 0; j < numCmds; j++) {
                        int textureAllocationIndex = (int) drawData.getCmdListCmdBufferTextureId(i, j);
                        int elemCount = drawData.getCmdListCmdBufferElemCount(i, j);
                        int indexBufferOffset = drawData.getCmdListCmdBufferIdxOffset(i, j);

                        ByteBuffer textureAllocationIndexBuffer = stack.calloc(1 * Integer.BYTES);
                        textureAllocationIndexBuffer.putInt(textureAllocationIndex);
                        textureAllocationIndexBuffer.rewind();
                        vkCmdPushConstants(
                                commandBuffer,
                                pipelineLayout,
                                VK_SHADER_STAGE_FRAGMENT_BIT,
                                1 * 16 * Float.BYTES + 1 * Integer.BYTES,
                                textureAllocationIndexBuffer
                        );

                        vkCmdDrawIndexed(commandBuffer, elemCount, 1, indexBufferOffset, 0, 0);
                    }
                } else {
                    int numIndices = numIndicesMap.get(i);
                    vkCmdDrawIndexed(commandBuffer, numIndices, 1, 0, 0, 0);
                }
            }
        }
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isValid() || !this.isVisible() || drawData == null) {
            return;
        }

        this.recordCommands(commandBuffer, pipelineLayout);
    }

    @Override
    public void transfer(VkCommandBuffer commandBuffer) {
        if (!this.isValid() || !this.isVisible() || drawData == null) {
            return;
        }

        this.recordCommands(commandBuffer, null);
    }

    @Override
    public void cleanupLocally() {
        vertexBuffers.values().forEach(v -> vkDestroyBuffer(device, v, null));
        vertexBufferMemories.values().forEach(v -> vkFreeMemory(device, v, null));
        indexBuffers.values().forEach(v -> vkDestroyBuffer(device, v, null));
        indexBufferMemories.values().forEach(v -> vkFreeMemory(device, v, null));

        vertexBuffers.clear();
        vertexBufferMemories.clear();
        indexBuffers.clear();
        indexBufferMemories.clear();
        numIndicesMap.clear();

        drawData = null;
    }
}
