package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for fog
 *
 * @author maeda
 */
public class FogUBO {
    public static final int SIZEOF = (1 * 3 + 2 * 1) * Float.BYTES;

    private Vector3f color;
    private float start;
    private float end;

    public FogUBO() {
        color = new Vector3f();
        start = 0.0f;
        end = 0.0f;
    }

    public FogUBO(Fog fog) {
        color = fog.getColor();
        start = fog.getStart();
        end = fog.getEnd();
    }

    private void memcpy(ByteBuffer buffer) {
        final int vec3Size = 3 * Float.BYTES;
        final int floatSize = Float.BYTES;

        color.get(0, buffer);
        buffer.putFloat(vec3Size, start);
        buffer.putFloat(vec3Size + floatSize, end);

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
