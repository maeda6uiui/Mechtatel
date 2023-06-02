package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.postprocessing.light.LightingInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Vector3f;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC3;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;

/**
 * Uniform buffer object for lighting info
 *
 * @author maeda6uiui
 */
public class LightingInfoUBO extends UBO {
    public static final int SIZEOF = SIZEOF_VEC4 * 3;

    private Vector3f ambientColor;
    private Vector3f lightingClampMin;
    private Vector3f lightingClampMax;
    private int numLights;

    public LightingInfoUBO(LightingInfo lightingInfo) {
        ambientColor = lightingInfo.getAmbientColor();
        lightingClampMin = lightingInfo.getLightingClampMin();
        lightingClampMax = lightingInfo.getLightingClampMax();
        numLights = lightingInfo.getNumLights();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        ambientColor.get(0, buffer);
        lightingClampMin.get(SIZEOF_VEC4, buffer);
        lightingClampMax.get(SIZEOF_VEC4 * 2, buffer);
        buffer.putInt(SIZEOF_VEC4 * 2 + SIZEOF_VEC3, numLights);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}