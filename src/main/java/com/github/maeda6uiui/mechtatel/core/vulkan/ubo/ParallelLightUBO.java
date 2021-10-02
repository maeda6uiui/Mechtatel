package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
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
 * Uniform buffer object for a parallel light
 *
 * @author maeda
 */
public class ParallelLightUBO {
    public static final int SIZEOF = 10 * SIZEOF_VEC4;

    private Vector3f direction;
    private Vector3f ambientColor;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private Vector3f ambientClampMin;
    private Vector3f ambientClampMax;
    private Vector3f diffuseClampMin;
    private Vector3f diffuseClampMax;
    private Vector3f specularClampMin;
    private Vector3f specularClampMax;
    private float specularPowY;

    public ParallelLightUBO(ParallelLight light) {
        direction = light.getDirection();
        ambientColor = light.getAmbientColor();
        diffuseColor = light.getDiffuseColor();
        specularColor = light.getSpecularColor();
        ambientClampMin = light.getAmbientClampMin();
        ambientClampMax = light.getAmbientClampMax();
        diffuseClampMin = light.getDiffuseClampMin();
        diffuseClampMax = light.getDiffuseClampMax();
        specularClampMin = light.getSpecularClampMin();
        specularClampMax = light.getSpecularClampMax();
        specularPowY = light.getSpecularPowY();
    }

    private void memcpy(ByteBuffer buffer) {
        direction.get(0, buffer);
        ambientColor.get(SIZEOF_VEC4, buffer);
        diffuseColor.get(SIZEOF_VEC4 * 2, buffer);
        specularColor.get(SIZEOF_VEC4 * 3, buffer);
        ambientClampMin.get(SIZEOF_VEC4 * 4, buffer);
        ambientClampMax.get(SIZEOF_VEC4 * 5, buffer);
        diffuseClampMin.get(SIZEOF_VEC4 * 6, buffer);
        diffuseClampMax.get(SIZEOF_VEC4 * 7, buffer);
        specularClampMin.get(SIZEOF_VEC4 * 8, buffer);
        specularClampMax.get(SIZEOF_VEC4 * 9, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 9 + SIZEOF_VEC3, specularPowY);

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
