package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

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
 * @author maeda6uiui
 */
public class ParallelLightUBO {
    public static final int SIZEOF = 7 * SIZEOF_VEC4;

    private Vector3f direction;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private Vector3f diffuseClampMin;
    private Vector3f diffuseClampMax;
    private Vector3f specularClampMin;
    private Vector3f specularClampMax;
    private float specularPowY;

    public ParallelLightUBO(ParallelLight parallelLight) {
        direction = parallelLight.getDirection();
        diffuseColor = parallelLight.getDiffuseColor();
        specularColor = parallelLight.getSpecularColor();
        diffuseClampMin = parallelLight.getDiffuseClampMin();
        diffuseClampMax = parallelLight.getDiffuseClampMax();
        specularClampMin = parallelLight.getSpecularClampMin();
        specularClampMax = parallelLight.getSpecularClampMax();
        specularPowY = parallelLight.getSpecularPowY();
    }

    private void memcpy(ByteBuffer buffer) {
        direction.get(0, buffer);
        diffuseColor.get(SIZEOF_VEC4, buffer);
        specularColor.get(SIZEOF_VEC4 * 2, buffer);
        diffuseClampMin.get(SIZEOF_VEC4 * 3, buffer);
        diffuseClampMax.get(SIZEOF_VEC4 * 4, buffer);
        specularClampMin.get(SIZEOF_VEC4 * 5, buffer);
        specularClampMax.get(SIZEOF_VEC4 * 6, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 6 + SIZEOF_VEC3, specularPowY);

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
