package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.postprocessing.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC3;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;

/**
 * Uniform buffer object for a parallel light
 *
 * @author maeda6uiui
 */
public class ParallelLightUBO extends UBO {
    public static final int SIZEOF = SIZEOF_VEC4 * 7;

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

    @Override
    protected void memcpy(ByteBuffer buffer) {
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

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
