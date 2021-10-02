package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.light.LightingInfo;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.*;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for lighting info
 *
 * @author maeda
 */
public class LightingInfoUBO {
    public static final int SIZEOF = SIZEOF_VEC4 + SIZEOF_VEC3 + SIZEOF_INT;

    private Vector3f lightingClampMin;
    private Vector3f lightingClampMax;
    private int numLights;

    public LightingInfoUBO() {
        lightingClampMin = new Vector3f();
        lightingClampMax = new Vector3f();
        numLights = 0;
    }

    public LightingInfoUBO(LightingInfo lightingInfo) {
        lightingClampMin = lightingInfo.getLightingClampMin();
        lightingClampMax = lightingInfo.getLightingClampMax();
        numLights = lightingInfo.getNumLights();
    }

    private void memcpy(ByteBuffer buffer) {
        lightingClampMin.get(0, buffer);
        lightingClampMax.get(SIZEOF_VEC4, buffer);
        buffer.putInt(SIZEOF_VEC4 + SIZEOF_VEC3, numLights);

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