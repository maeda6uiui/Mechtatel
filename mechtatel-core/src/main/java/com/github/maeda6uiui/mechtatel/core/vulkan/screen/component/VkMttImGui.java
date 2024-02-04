package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.texture.VkMttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.BufferUtils;
import imgui.ImDrawData;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkDevice;
import org.lwjgl.vulkan.VkQueue;

import java.nio.ByteBuffer;
import java.nio.LongBuffer;
import java.util.HashMap;
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

    private VkMttTexture texture;
    private ImDrawData drawData;

    private Map<Integer, Long> vertexBuffers;
    private Map<Integer, Long> vertexBufferMemories;
    private Map<Integer, Long> indexBuffers;
    private Map<Integer, Long> indexBufferMemories;

    private boolean isExternalTexture;

    public VkMttImGui(
            IMttComponentForVkMttComponent mttComponent,
            VkDevice device,
            long commandPool,
            VkQueue graphicsQueue,
            VkMttScreen screen,
            VkMttTexture texture) {
        super(mttComponent, screen, "gbuffer");

        this.device = device;
        this.commandPool = commandPool;
        this.graphicsQueue = graphicsQueue;

        this.texture = texture;
        isExternalTexture = true;

        vertexBuffers = new HashMap<>();
        vertexBufferMemories = new HashMap<>();
        indexBuffers = new HashMap<>();
        indexBufferMemories = new HashMap<>();
    }

    @Override
    public void cleanup() {
        if (!isExternalTexture) {
            texture.cleanup();
        }

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

    private void recordCommands(VkCommandBuffer commandBuffer) {
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
                    BufferUtils.BufferInfo bufferInfo = BufferUtils.createByteBuffer(
                            device,
                            commandPool,
                            graphicsQueue,
                            drawData.getCmdListVtxBufferData(i)
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
                    BufferUtils.BufferInfo bufferInfo = BufferUtils.createByteBuffer(
                            device,
                            commandPool,
                            graphicsQueue,
                            drawData.getCmdListIdxBufferData(i)
                    );
                    indexBuffer = bufferInfo.buffer;

                    indexBuffers.put(i, bufferInfo.buffer);
                    indexBufferMemories.put(i, bufferInfo.bufferMemory);
                }

                //Bind vertex buffer
                LongBuffer lVertexBuffers = stack.longs(vertexBuffer);
                LongBuffer offsets = stack.longs(0);
                vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

                //Bind index buffer
                vkCmdBindIndexBuffer(commandBuffer, indexBuffer, 0, VK_INDEX_TYPE_UINT16);

                //Record commands
                int numCmds = drawData.getCmdListCmdBufferSize(i);
                for (int j = 0; j < numCmds; j++) {
                    int elemCount = drawData.getCmdListCmdBufferElemCount(i, j);
                    int idxBufferOffset = drawData.getCmdListCmdBufferIdxOffset(i, j);
                    int firstIndex = idxBufferOffset * ImDrawData.SIZEOF_IM_DRAW_IDX;

                    vkCmdDrawIndexed(commandBuffer, elemCount, 1, firstIndex, 0, 0);
                }
            }
        }
    }

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isVisible() || texture == null || drawData == null) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            //Bind texture
            ByteBuffer textureAllocationIndexBuffer = stack.calloc(1 * Integer.BYTES);
            textureAllocationIndexBuffer.putInt(texture.getAllocationIndex());
            textureAllocationIndexBuffer.rewind();

            vkCmdPushConstants(
                    commandBuffer,
                    pipelineLayout,
                    VK_SHADER_STAGE_FRAGMENT_BIT,
                    1 * 16 * Float.BYTES + 1 * Integer.BYTES,
                    textureAllocationIndexBuffer);

            //Record commands
            this.recordCommands(commandBuffer);
        }
    }

    @Override
    public void transfer(VkCommandBuffer commandBuffer) {
        if (!this.isVisible() || drawData == null) {
            return;
        }

        this.recordCommands(commandBuffer);
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

        drawData = null;
    }
}
