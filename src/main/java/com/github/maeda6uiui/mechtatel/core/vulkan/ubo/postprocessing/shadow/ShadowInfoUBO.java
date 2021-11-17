package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.shadow.ShadowInfo;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_MAT4;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for shadow info
 *
 * @author maeda
 */
public class ShadowInfoUBO {
    public static final int SIZEOF = 2 * SIZEOF_MAT4 + 3 * SIZEOF_VEC4;

    private Matrix4f lightView;
    private Matrix4f lightProj;
    private Vector3f lightDirection;
    private Vector3f attenuations;
    private int projectionType;

    public ShadowInfoUBO(ShadowInfo info) {
        lightView = info.getLightView();
        lightProj = info.getLightProj();
        lightDirection = info.getLightDirection();
        attenuations = info.getAttenuations();
        projectionType = info.getProjectionType();
    }

    private void memcpy(ByteBuffer buffer) {
        lightView.get(0, buffer);
        lightProj.get(SIZEOF_MAT4 * 1, buffer);
        lightDirection.get(SIZEOF_MAT4 * 2, buffer);
        attenuations.get(SIZEOF_MAT4 * 2 + SIZEOF_VEC4 * 1, buffer);
        buffer.putInt(SIZEOF_MAT4 * 2 + SIZEOF_VEC4 * 2, projectionType);

        buffer.rewind();
    }

    public void update(VkDevice device, long uniformBufferMemory, int index) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            PointerBuffer data = stack.mallocPointer(1);
            vkMapMemory(device, uniformBufferMemory, index * SIZEOF, SIZEOF, 0, data);
            {
                this.memcpy(data.getByteBuffer(0, SIZEOF));
            }
            vkUnmapMemory(device, uniformBufferMemory);
        }
    }
}
