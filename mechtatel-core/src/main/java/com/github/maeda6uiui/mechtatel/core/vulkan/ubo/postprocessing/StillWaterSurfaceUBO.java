package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.postprocessing.water.StillWaterSurface;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_FLOAT;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;

/**
 * Uniform buffer object for still water surface
 *
 * @author maeda6uiui
 */
public class StillWaterSurfaceUBO extends UBO {
    public static final int SIZEOF = SIZEOF_VEC4 * 5;

    private Vector3f shallowColor;
    private float waterLevel;
    private Vector3f deepColor;
    private float distortionStrength;
    private Vector3f sunDirection;
    private float waveAmplitude;
    private float waveFrequency;
    private float waveSpeed;
    private float absorptionCoefficient;
    private float specularStrength;
    private float time;

    public StillWaterSurfaceUBO(StillWaterSurface stillWaterSurface) {
        shallowColor = stillWaterSurface.getShallowColor();
        waterLevel = stillWaterSurface.getWaterLevel();
        deepColor = stillWaterSurface.getDeepColor();
        distortionStrength = stillWaterSurface.getDistortionStrength();
        sunDirection = stillWaterSurface.getSunDirection();
        waveAmplitude = stillWaterSurface.getWaveAmplitude();
        waveFrequency = stillWaterSurface.getWaveFrequency();
        waveSpeed = stillWaterSurface.getWaveSpeed();
        absorptionCoefficient = stillWaterSurface.getAbsorptionCoefficient();
        specularStrength = stillWaterSurface.getSpecularStrength();
        time = (float) GLFW.glfwGetTime();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        shallowColor.get(0, buffer);
        buffer.putFloat(SIZEOF_FLOAT * 3, waterLevel);

        deepColor.get(SIZEOF_VEC4, buffer);
        buffer.putFloat(SIZEOF_VEC4 + SIZEOF_FLOAT * 3, distortionStrength);

        sunDirection.get(SIZEOF_VEC4 * 2, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 2 + SIZEOF_FLOAT * 3, waveAmplitude);

        buffer.putFloat(SIZEOF_VEC4 * 3, waveFrequency);
        buffer.putFloat(SIZEOF_VEC4 * 3 + SIZEOF_FLOAT, waveSpeed);
        buffer.putFloat(SIZEOF_VEC4 * 3 + SIZEOF_FLOAT * 2, absorptionCoefficient);
        buffer.putFloat(SIZEOF_VEC4 * 3 + SIZEOF_FLOAT * 3, specularStrength);

        buffer.putFloat(SIZEOF_VEC4 * 4, time);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}