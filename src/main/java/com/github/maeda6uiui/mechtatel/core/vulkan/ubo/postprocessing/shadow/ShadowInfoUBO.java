package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing.shadow;

import com.github.maeda6uiui.mechtatel.core.postprocessing.shadow.ShadowInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.*;

/**
 * Uniform buffer object for shadow info
 *
 * @author maeda6uiui
 */
public class ShadowInfoUBO extends UBO {
    public static final int SIZEOF = SIZEOF_MAT4 * 2 + SIZEOF_VEC4 * 2;

    private Matrix4f lightView;
    private Matrix4f lightProj;
    private Vector3f lightDirection;
    private Vector3f attenuations;
    private int projectionType;

    public ShadowInfoUBO(ShadowInfo info) {
        lightView = info.getLightView();
        lightProj = info.getLightProj();
        lightDirection = info.getLightDirection();
        attenuations = info.getAttenuations();
        projectionType = info.getProjectionType();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        lightView.get(0, buffer);
        lightProj.get(SIZEOF_MAT4 * 1, buffer);
        lightDirection.get(SIZEOF_MAT4 * 2, buffer);
        attenuations.get(SIZEOF_MAT4 * 2 + SIZEOF_VEC4 * 1, buffer);
        buffer.putInt(SIZEOF_MAT4 * 2 + SIZEOF_VEC4 * 1 + SIZEOF_VEC3 * 1, projectionType);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
