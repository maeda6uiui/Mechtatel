package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.shadow.Pass1Info;
import org.joml.Matrix4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_MAT4;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for pass 1 info
 *
 * @author maeda6uiui
 */
public class Pass1InfoUBO {
    public static final int SIZEOF = 2 * SIZEOF_MAT4;

    private Matrix4f lightView;
    private Matrix4f lightProj;

    public Pass1InfoUBO(Pass1Info info) {
        lightView = info.getLightView();
        lightProj = info.getLightProj();
    }

    private void memcpy(ByteBuffer buffer) {
        lightView.get(0, buffer);
        lightProj.get(1 * SIZEOF_MAT4, buffer);

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
