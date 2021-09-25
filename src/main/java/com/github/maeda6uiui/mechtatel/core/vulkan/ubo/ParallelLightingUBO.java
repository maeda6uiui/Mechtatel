package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.vulkan.util.AlignmentUtils;
import org.joml.Vector3f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkDevice;

import java.nio.ByteBuffer;
import java.util.List;

import static org.lwjgl.vulkan.VK10.vkMapMemory;
import static org.lwjgl.vulkan.VK10.vkUnmapMemory;

/**
 * Uniform buffer object for a parallel light
 *
 * @author maeda
 */
public class ParallelLightingUBO {
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
    private float speculatPowY;

    public ParallelLightingUBO() {
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
        speculatPowY = 0.0f;
    }

    public ParallelLightingUBO(ParallelLight light) {
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
        speculatPowY = light.getSpeculatPowY();
    }

    private void memcpy(ByteBuffer buffer) {
        final int vec4Size = 4 * Float.BYTES;

        direction.get(0, buffer);
        ambientColor.get(AlignmentUtils.alignas(vec4Size, AlignmentUtils.alignof(ambientColor)), buffer);
        diffuseColor.get((AlignmentUtils.alignas(vec4Size * 2, AlignmentUtils.alignof(diffuseColor))), buffer);
        specularColor.get((AlignmentUtils.alignas(vec4Size * 3, AlignmentUtils.alignof(specularColor))), buffer);
        ambientClampMin.get((AlignmentUtils.alignas(vec4Size * 4, AlignmentUtils.alignof(ambientClampMin))), buffer);
        ambientClampMax.get((AlignmentUtils.alignas(vec4Size * 5, AlignmentUtils.alignof(ambientClampMax))), buffer);
        diffuseClampMin.get((AlignmentUtils.alignas(vec4Size * 6, AlignmentUtils.alignof(diffuseClampMin))), buffer);
        diffuseClampMax.get((AlignmentUtils.alignas(vec4Size * 7, AlignmentUtils.alignof(diffuseClampMax))), buffer);
        specularClampMin.get((AlignmentUtils.alignas(vec4Size * 8, AlignmentUtils.alignof(specularClampMin))), buffer);
        specularClampMax.get((AlignmentUtils.alignas(vec4Size * 9, AlignmentUtils.alignof(specularClampMax))), buffer);
        buffer.putFloat(AlignmentUtils.alignas(vec4Size * 10, AlignmentUtils.alignof(speculatPowY)), speculatPowY);
    }

    public void update(
            VkDevice device,
            List<Long> uniformBufferMemories) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            for (var uniformBufferMemory : uniformBufferMemories) {
                PointerBuffer data = stack.mallocPointer(1);
                vkMapMemory(device, uniformBufferMemory, 0, ParallelLightingUBO.SIZEOF, 0, data);
                {
                    this.memcpy(data.getByteBuffer(0, ParallelLightingUBO.SIZEOF));
                }
                vkUnmapMemory(device, uniformBufferMemory);
            }
        }
    }
}
