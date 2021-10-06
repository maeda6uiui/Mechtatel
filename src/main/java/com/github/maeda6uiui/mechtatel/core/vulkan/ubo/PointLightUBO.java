package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.*;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for a point light
 *
 * @author maeda
 */
public class PointLightUBO {
    public static final int SIZEOF = 5 * SIZEOF_VEC4;

    private Vector3f position;
    private Vector3f diffuseColor;
    private Vector3f diffuseClampMin;
    private Vector3f diffuseClampMax;
    private float k0;
    private float k1;
    private float k2;

    public PointLightUBO(PointLight pointLight) {
        position = pointLight.getPosition();
        diffuseColor = pointLight.getDiffuseColor();
        diffuseClampMin = pointLight.getDiffuseClampMin();
        diffuseClampMax = pointLight.getDiffuseClampMax();
        k0 = pointLight.getK0();
        k1 = pointLight.getK1();
        k2 = pointLight.getK2();
    }

    private void memcpy(ByteBuffer buffer) {
        position.get(0, buffer);
        diffuseColor.get(SIZEOF_VEC4 * 1, buffer);
        diffuseClampMin.get(SIZEOF_VEC4 * 2, buffer);
        diffuseClampMax.get(SIZEOF_VEC4 * 3, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 3 + SIZEOF_VEC3, k0);
        buffer.putFloat(SIZEOF_VEC4 * 4, k1);
        buffer.putFloat(SIZEOF_VEC4 * 4 + SIZEOF_FLOAT, k2);

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
