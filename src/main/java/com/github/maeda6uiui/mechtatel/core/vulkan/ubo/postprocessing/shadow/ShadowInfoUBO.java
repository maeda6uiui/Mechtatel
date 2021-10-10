package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.shadow.ShadowInfo;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC3;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for shadow info
 *
 * @author maeda
 */
public class ShadowInfoUBO {
    public static final int SIZEOF = 3 * SIZEOF_VEC4;

    private int projectionType;
    private Vector3f lightDirection;
    private Vector3f attenuations;
    private float biasCoefficient;
    private float maxBias;

    public ShadowInfoUBO(ShadowInfo info) {
        projectionType = info.getProjectionType();
        lightDirection = info.getLightDirection();
        attenuations = info.getAttenuations();
        biasCoefficient = info.getBiasCoefficient();
        maxBias = info.getMaxBias();
    }

    private void memcpy(ByteBuffer buffer) {
        buffer.putInt(0, projectionType);
        lightDirection.get(Integer.BYTES * 1, buffer);
        attenuations.get(SIZEOF_VEC4 * 1, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 1 + SIZEOF_VEC3, biasCoefficient);
        buffer.putFloat(SIZEOF_VEC4 * 2, maxBias);

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
