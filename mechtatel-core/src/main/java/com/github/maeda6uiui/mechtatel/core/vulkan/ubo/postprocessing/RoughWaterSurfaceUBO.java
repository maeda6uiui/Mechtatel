package com.github.maeda6uiui.mechtatel.core.vulkan.ubo.postprocessing;

import com.github.maeda6uiui.mechtatel.core.postprocessing.water.RoughWaterSurface;
import com.github.maeda6uiui.mechtatel.core.vulkan.ubo.UBO;
import org.joml.Vector3f;
import org.lwjgl.glfw.GLFW;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_FLOAT;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;

/**
 * Uniform buffer object for rough water surface
 *
 * <p>The layout mirrors the {@code WaterSurfaceUBO} struct in
 * {@code Standard/Shader/PostProcessing/RoughWaterSurface/main.frag.slang}:
 * six std140 vec4 blocks (each a vec3 plus a trailing scalar) followed by four
 * loose floats.
 *
 * @author maeda6uiui
 */
public class RoughWaterSurfaceUBO extends UBO {
    public static final int SIZEOF = SIZEOF_VEC4 * 6 + SIZEOF_FLOAT * 4;

    private Vector3f deepColor;
    private float waterLevel;
    private Vector3f shallowColor;
    private float transmissionStrength;
    private Vector3f sunDirection;
    private float waveAmplitude;
    private Vector3f horizonColor;
    private float waveFrequency;
    private Vector3f zenithColor;
    private float waveSpeed;
    private Vector3f foamColor;
    private float foamThreshold;
    private float choppiness;
    private float foamIntensity;
    private float specularStrength;
    private float time;

    public RoughWaterSurfaceUBO(RoughWaterSurface roughWaterSurface) {
        deepColor = roughWaterSurface.getDeepColor();
        waterLevel = roughWaterSurface.getWaterLevel();
        shallowColor = roughWaterSurface.getShallowColor();
        transmissionStrength = roughWaterSurface.getTransmissionStrength();
        sunDirection = roughWaterSurface.getSunDirection();
        waveAmplitude = roughWaterSurface.getWaveAmplitude();
        horizonColor = roughWaterSurface.getHorizonColor();
        waveFrequency = roughWaterSurface.getWaveFrequency();
        zenithColor = roughWaterSurface.getZenithColor();
        waveSpeed = roughWaterSurface.getWaveSpeed();
        foamColor = roughWaterSurface.getFoamColor();
        foamThreshold = roughWaterSurface.getFoamThreshold();
        choppiness = roughWaterSurface.getChoppiness();
        foamIntensity = roughWaterSurface.getFoamIntensity();
        specularStrength = roughWaterSurface.getSpecularStrength();
        time = (float) GLFW.glfwGetTime();
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        deepColor.get(0, buffer);
        buffer.putFloat(SIZEOF_FLOAT * 3, waterLevel);

        shallowColor.get(SIZEOF_VEC4, buffer);
        buffer.putFloat(SIZEOF_VEC4 + SIZEOF_FLOAT * 3, transmissionStrength);

        sunDirection.get(SIZEOF_VEC4 * 2, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 2 + SIZEOF_FLOAT * 3, waveAmplitude);

        horizonColor.get(SIZEOF_VEC4 * 3, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 3 + SIZEOF_FLOAT * 3, waveFrequency);

        zenithColor.get(SIZEOF_VEC4 * 4, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 4 + SIZEOF_FLOAT * 3, waveSpeed);

        foamColor.get(SIZEOF_VEC4 * 5, buffer);
        buffer.putFloat(SIZEOF_VEC4 * 5 + SIZEOF_FLOAT * 3, foamThreshold);

        buffer.putFloat(SIZEOF_VEC4 * 6, choppiness);
        buffer.putFloat(SIZEOF_VEC4 * 6 + SIZEOF_FLOAT, foamIntensity);
        buffer.putFloat(SIZEOF_VEC4 * 6 + SIZEOF_FLOAT * 2, specularStrength);
        buffer.putFloat(SIZEOF_VEC4 * 6 + SIZEOF_FLOAT * 3, time);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
