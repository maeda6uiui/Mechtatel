package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.postprocessing.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_FLOAT;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC3;

/**
 * Uniform buffer object for fog
 *
 * @author maeda6uiui
 */
public class FogUBO extends UBO {
    public static final int SIZEOF = SIZEOF_VEC3 + SIZEOF_FLOAT * 2;

    private Vector3f color;
    private float start;
    private float end;

    public FogUBO(Fog fog) {
        color = fog.getColor();
        start = fog.getStart();
        end = fog.getEnd();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        color.get(0, buffer);
        buffer.putFloat(SIZEOF_VEC3, start);
        buffer.putFloat(SIZEOF_VEC3 + SIZEOF_FLOAT, end);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
