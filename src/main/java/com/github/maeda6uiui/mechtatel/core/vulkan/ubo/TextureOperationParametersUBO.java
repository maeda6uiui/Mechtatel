package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.texture.TextureOperationParameters;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_INT;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for texture operation parameters
 *
 * @author maeda6uiui
 */
public class TextureOperationParametersUBO {
    public static final int SIZEOF = 2 * SIZEOF_VEC4 + SIZEOF_INT;

    private Vector4f firstTextureFactor;
    private Vector4f secondTextureFactor;
    private int operationType;

    public TextureOperationParametersUBO(TextureOperationParameters parameters) {
        firstTextureFactor = parameters.getFirstTextureFactor();
        secondTextureFactor = parameters.getSecondTextureFactor();
        operationType = parameters.getOperationType();
    }

    private void memcpy(ByteBuffer buffer) {
        firstTextureFactor.get(0, buffer);
        secondTextureFactor.get(SIZEOF_VEC4, buffer);
        buffer.putInt(SIZEOF_VEC4 * 2, operationType);

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
