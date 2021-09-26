package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;

import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for a parallel light
 *
 * @author maeda
 */
public class ParallelLightUBO {
    public static final int SIZEOF = (10 * 4 + 1 * 1) * Float.BYTES;

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

    public ParallelLightUBO() {
        direction = new Vector3f();
        ambientColor = new Vector3f();
        diffuseColor = new Vector3f();
        specularColor = new Vector3f();
        ambientClampMin = new Vector3f();
        ambientClampMax = new Vector3f();
        diffuseClampMin = new Vector3f();
        diffuseClampMax = new Vector3f();
        specularClampMin = new Vector3f();
        specularClampMax = new Vector3f();
        specularPowY = 0.0f;
    }

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
        final int vec3Size = 3 * Float.BYTES;
        final int vec4Size = 4 * Float.BYTES;

        direction.get(0, buffer);
        ambientColor.get(vec4Size, buffer);
        diffuseColor.get(vec4Size * 2, buffer);
        specularColor.get(vec4Size * 3, buffer);
        ambientClampMin.get(vec4Size * 4, buffer);
        ambientClampMax.get(vec4Size * 5, buffer);
        diffuseClampMin.get(vec4Size * 6, buffer);
        diffuseClampMax.get(vec4Size * 7, buffer);
        specularClampMin.get(vec4Size * 8, buffer);
        specularClampMax.get(vec4Size * 9, buffer);
        buffer.putFloat(vec4Size * 9 + vec3Size, specularPowY);

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
