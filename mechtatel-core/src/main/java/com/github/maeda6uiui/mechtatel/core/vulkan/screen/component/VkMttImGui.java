package com.github.maeda6uiui.mechtatel.core.vulkan.screen.component;

import com.github.maeda6uiui.mechtatel.core.screen.component.IMttComponentForVkMttComponent;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttVertexUV;
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
import java.util.List;

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

    private long vertexBuffer;
    private long vertexBufferMemory;
    private long indexBuffer;
    private long indexBufferMemory;
    private boolean bufferCreated;
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

        //Create empty vertex buffer
        BufferUtils.BufferInfo bufferInfo = BufferUtils.createVertexBufferUV(
                device, commandPool, graphicsQueue, List.of(new MttVertexUV())
        );
        vertexBuffer = bufferInfo.buffer;
        vertexBufferMemory = bufferInfo.bufferMemory;

        //Create empty index buffer
        bufferInfo = BufferUtils.createIndexBuffer(
                device, commandPool, graphicsQueue, List.of(0)
        );
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

    @Override
    public void draw(VkCommandBuffer commandBuffer, long pipelineLayout) {
        if (!this.isVisible() || texture == null || !bufferCreated || drawData == null) {
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

            int numLists = drawData.getCmdListsCount();
            for (int i = 0; i < numLists; i++) {
                //Update vertex and index buffers
                BufferUtils.updateBuffer(
                        device,
                        commandPool,
                        graphicsQueue,
                        vertexBuffer,
                        drawData.getCmdListVtxBufferData(i)
                );
                BufferUtils.updateBuffer(
                        device,
                        commandPool,
                        graphicsQueue,
                        indexBuffer,
                        drawData.getCmdListIdxBufferData(i)
                );

                //Record commands
                int numCmds = drawData.getCmdListCmdBufferSize(i);
                for (int j = 0; j < numCmds; j++) {
                    //Bind vertex buffer
                    LongBuffer lVertexBuffers = stack.longs(vertexBuffer);
                    LongBuffer offsets = stack.longs(0);
                    vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

                    //Bind index buffer
                    vkCmdBindIndexBuffer(commandBuffer, indexBuffer, 0, VK_INDEX_TYPE_UINT16);

                    //Draw
                    int elemCount = drawData.getCmdListCmdBufferElemCount(i, j);
                    int idxBufferOffset = drawData.getCmdListCmdBufferIdxOffset(i, j);
                    int firstIndex = idxBufferOffset * ImDrawData.SIZEOF_IM_DRAW_IDX;

                    vkCmdDrawIndexed(commandBuffer, elemCount, 1, firstIndex, 0, 0);
                }
            }
        }

        drawData = null;
    }

    @Override
    public void transfer(VkCommandBuffer commandBuffer) {
        if (!this.isVisible() || !bufferCreated || drawData == null) {
            return;
        }

        try (MemoryStack stack = MemoryStack.stackPush()) {
            int numLists = drawData.getCmdListsCount();
            for (int i = 0; i < numLists; i++) {
                //Update vertex and index buffers
                BufferUtils.updateBuffer(
                        device,
                        commandPool,
                        graphicsQueue,
                        vertexBuffer,
                        drawData.getCmdListVtxBufferData(i)
                );
                BufferUtils.updateBuffer(
                        device,
                        commandPool,
                        graphicsQueue,
                        indexBuffer,
                        drawData.getCmdListIdxBufferData(i)
                );

                //Record commands
                int numCmds = drawData.getCmdListCmdBufferSize(i);
                for (int j = 0; j < numCmds; j++) {
                    //Bind vertex buffer
                    LongBuffer lVertexBuffers = stack.longs(vertexBuffer);
                    LongBuffer offsets = stack.longs(0);
                    vkCmdBindVertexBuffers(commandBuffer, 0, lVertexBuffers, offsets);

                    //Bind index buffer
                    vkCmdBindIndexBuffer(commandBuffer, indexBuffer, 0, VK_INDEX_TYPE_UINT16);

                    //Draw
                    int elemCount = drawData.getCmdListCmdBufferElemCount(i, j);
                    int idxBufferOffset = drawData.getCmdListCmdBufferIdxOffset(i, j);
                    int firstIndex = idxBufferOffset * ImDrawData.SIZEOF_IM_DRAW_IDX;

                    vkCmdDrawIndexed(commandBuffer, elemCount, 1, firstIndex, 0, 0);
                }
            }
        }
    }
}
