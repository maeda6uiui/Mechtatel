package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.blur.SimpleBlurInfo;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_INT;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for simple blur info
 *
 * @author maeda6uiui
 */
public class SimpleBlurInfoUBO {
    public static final int SIZEOF = SIZEOF_INT * 4;

    private int textureWidth;
    private int textureHeight;
    private int blurSize;
    private int stride;

    public SimpleBlurInfoUBO(SimpleBlurInfo blurInfo) {
        textureWidth = blurInfo.getTextureWidth();
        textureHeight = blurInfo.getTextureHeight();
        blurSize = blurInfo.getBlurSize();
        stride = blurInfo.getStride();
    }

    private void memcpy(ByteBuffer buffer) {
        buffer.putInt(0, textureWidth);
        buffer.putInt(SIZEOF_INT, textureHeight);
        buffer.putInt(SIZEOF_INT * 2, blurSize);
        buffer.putInt(SIZEOF_INT * 3, stride);

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
