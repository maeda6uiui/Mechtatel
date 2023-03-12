package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.light.LightingInfo;
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
 * Uniform buffer object for lighting info
 *
 * @author maeda6uiui
 */
public class LightingInfoUBO {
    public static final int SIZEOF = 3 * SIZEOF_VEC4;

    private Vector3f ambientColor;
    private Vector3f lightingClampMin;
    private Vector3f lightingClampMax;
    private int numLights;

    public LightingInfoUBO(LightingInfo lightingInfo) {
        ambientColor = lightingInfo.getAmbientColor();
        lightingClampMin = lightingInfo.getLightingClampMin();
        lightingClampMax = lightingInfo.getLightingClampMax();
        numLights = lightingInfo.getNumLights();
    }

    private void memcpy(ByteBuffer buffer) {
        ambientColor.get(0, buffer);
        lightingClampMin.get(SIZEOF_VEC4, buffer);
        lightingClampMax.get(SIZEOF_VEC4 * 2, buffer);
        buffer.putInt(SIZEOF_VEC4 * 2 + SIZEOF_VEC3, numLights);

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