package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMatrices;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_MAT4;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for matrices for shadow mapping
 *
 * @author maeda
 */
public class ShadowMatricesUBO {
    public static final int SIZEOF = 2 * SIZEOF_MAT4;

    private Matrix4f view;
    private Matrix4f proj;

    public ShadowMatricesUBO(ShadowMatrices matrices) {
        view = matrices.getView();
        proj = matrices.getProj();
    }

    private void memcpy(ByteBuffer buffer) {
        view.get(0, buffer);
        proj.get(SIZEOF_MAT4 * 1, buffer);

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
