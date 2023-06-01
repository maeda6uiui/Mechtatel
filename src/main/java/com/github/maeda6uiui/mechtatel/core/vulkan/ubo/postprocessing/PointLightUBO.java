package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.*;

/**
 * Uniform buffer object for a point light
 *
 * @author maeda6uiui
 */
public class PointLightUBO extends UBO {
    public static final int SIZEOF = SIZEOF_VEC4 * 5;

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

    @Override
    protected void memcpy(ByteBuffer buffer) {
        position.get(0, buffer);
        diffuseColor.get(SIZEOF_VEC4 * 1, buffer);
        diffuseClampMin.get(SIZEOF_VEC4 * 2, buffer);
        diffuseClampMax.get(SIZEOF_VEC4 * 3, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 3 + SIZEOF_VEC3, k0);
        buffer.putFloat(SIZEOF_VEC4 * 4, k1);
        buffer.putFloat(SIZEOF_VEC4 * 4 + SIZEOF_FLOAT, k2);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
