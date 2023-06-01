package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.*;
import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for a spotlight
 *
 * @author maeda6uiui
 */
public class SpotlightUBO {
    public static final int SIZEOF = SIZEOF_VEC4 * 10;

    private Vector3f position;
    private Vector3f direction;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private Vector3f diffuseClampMin;
    private Vector3f diffuseClampMax;
    private Vector3f specularClampMin;
    private Vector3f specularClampMax;
    private float k0;
    private float k1;
    private float k2;
    private float theta;
    private float phi;
    private float falloff;
    private float specularPowY;

    public SpotlightUBO(Spotlight spotlight) {
        position = spotlight.getPosition();
        direction = spotlight.getDirection();
        diffuseColor = spotlight.getDiffuseColor();
        specularColor = spotlight.getSpecularColor();
        diffuseClampMin = spotlight.getDiffuseClampMin();
        diffuseClampMax = spotlight.getDiffuseClampMax();
        specularClampMin = spotlight.getSpecularClampMin();
        specularClampMax = spotlight.getSpecularClampMax();
        k0 = spotlight.getK0();
        k1 = spotlight.getK1();
        k2 = spotlight.getK2();
        theta = spotlight.getTheta();
        phi = spotlight.getPhi();
        falloff = spotlight.getFalloff();
        specularPowY = spotlight.getSpecularPowY();
    }

    private void memcpy(ByteBuffer buffer) {
        position.get(0, buffer);
        direction.get(SIZEOF_VEC4, buffer);
        diffuseColor.get(SIZEOF_VEC4 * 2, buffer);
        specularColor.get(SIZEOF_VEC4 * 3, buffer);
        diffuseClampMin.get(SIZEOF_VEC4 * 4, buffer);
        diffuseClampMax.get(SIZEOF_VEC4 * 5, buffer);
        specularClampMin.get(SIZEOF_VEC4 * 6, buffer);
        specularClampMax.get(SIZEOF_VEC4 * 7, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 7 + SIZEOF_VEC3, k0);
        buffer.putFloat(SIZEOF_VEC4 * 8, k1);
        buffer.putFloat(SIZEOF_VEC4 * 8 + SIZEOF_FLOAT, k2);
        buffer.putFloat(SIZEOF_VEC4 * 8 + SIZEOF_FLOAT * 2, theta);
        buffer.putFloat(SIZEOF_VEC4 * 8 + SIZEOF_FLOAT * 3, phi);
        buffer.putFloat(SIZEOF_VEC4 * 9, falloff);
        buffer.putFloat(SIZEOF_VEC4 * 9 + SIZEOF_FLOAT, specularPowY);

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
