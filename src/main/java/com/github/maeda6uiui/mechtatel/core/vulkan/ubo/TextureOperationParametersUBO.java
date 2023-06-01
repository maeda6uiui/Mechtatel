package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.texture.TextureOperationParameters;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.*;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for texture operation parameters
 *
 * @author maeda6uiui
 */
public class TextureOperationParametersUBO {
    public static final int SIZEOF = SIZEOF_VEC4 * 2 + SIZEOF_INT + SIZEOF_FLOAT * 2;

    private Vector4f firstTextureFactor;
    private Vector4f secondTextureFactor;
    private int operationType;
    private float firstTextureFixedDepth;
    private float secondTextureFixedDepth;

    public TextureOperationParametersUBO(TextureOperationParameters parameters) {
        firstTextureFactor = parameters.getFirstTextureFactor();
        secondTextureFactor = parameters.getSecondTextureFactor();
        operationType = parameters.getOperationType();
        firstTextureFixedDepth = parameters.getFirstTextureFixedDepth();
        secondTextureFixedDepth = parameters.getSecondTextureFixedDepth();
    }

    private void memcpy(ByteBuffer buffer) {
        firstTextureFactor.get(0, buffer);
        secondTextureFactor.get(SIZEOF_VEC4, buffer);
        buffer.putInt(SIZEOF_VEC4 * 2, operationType);
        buffer.putFloat(SIZEOF_VEC4 * 2 + SIZEOF_INT, firstTextureFixedDepth);
        buffer.putFloat(SIZEOF_VEC4 * 2 + SIZEOF_INT + SIZEOF_FLOAT, secondTextureFixedDepth);

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
