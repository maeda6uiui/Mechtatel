package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.shadow.Pass2Info;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_FLOAT;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for pass 2 info
 *
 * @author maeda6uiui
 */
public class Pass2InfoUBO {
    public static final int SIZEOF = 2 * SIZEOF_VEC4;

    private int numShadowMaps;
    private float biasCoefficient;
    private float maxBias;
    private float normalOffset;
    private int outputMode;
    private int outputDepthImageIndex;

    public Pass2InfoUBO(Pass2Info info) {
        numShadowMaps = info.getNumShadowMaps();
        biasCoefficient = info.getBiasCoefficient();
        maxBias = info.getMaxBias();
        normalOffset = info.getNormalOffset();
        outputMode = info.getOutputMode();
        outputDepthImageIndex = info.getOutputDepthImageIndex();
    }

    private void memcpy(ByteBuffer buffer) {
        buffer.putInt(0, numShadowMaps);
        buffer.putFloat(1 * SIZEOF_FLOAT, biasCoefficient);
        buffer.putFloat(2 * SIZEOF_FLOAT, maxBias);
        buffer.putFloat(3 * SIZEOF_FLOAT, normalOffset);
        buffer.putInt(1 * SIZEOF_VEC4, outputMode);
        buffer.putInt(1 * SIZEOF_VEC4 + 1 * SIZEOF_FLOAT, outputDepthImageIndex);

        buffer.rewind();
    }

    public void update(VkDevice device, long uniformBufferMemory) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(device, uniformBufferMemory, 0, SIZEOF, 0, data);
            {
                this.memcpy(data.getByteBuffer(0, SIZEOF));
            }
            vkUnmapMemory(device, uniformBufferMemory);
        }
    }
}
