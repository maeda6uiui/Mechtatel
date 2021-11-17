package com.github.maeda6uiui.mechtatel.core.shadow;

/**
 * Settings for shadow mapping
 *
 * @author maeda
 */
public class ShadowMappingSettings {
    public static final int OUTPUT_MODE_SHADOW_MAPPING = 0;
    public static final int OUTPUT_MODE_SHADOW_FACTORS = 1;
    public static final int OUTPUT_MODE_DEPTH_IMAGE = 2;

    private float biasCoefficient;
    private float maxBias;
    private float normalOffset;
    private int outputMode;
    private int outputDepthImageIndex;

    public ShadowMappingSettings() {
        biasCoefficient = 0.0001f;
        maxBias = 0.001f;
        normalOffset = 0.005f;
        outputMode = 0;
        outputDepthImageIndex = 0;
    }

    public float getBiasCoefficient() {
        return biasCoefficient;
    }

    public void setBiasCoefficient(float biasCoefficient) {
        this.biasCoefficient = biasCoefficient;
    }

    public float getMaxBias() {
        return maxBias;
    }

    public void setMaxBias(float maxBias) {
        this.maxBias = maxBias;
    }

    public float getNormalOffset() {
        return normalOffset;
    }

    public void setNormalOffset(float normalOffset) {
        this.normalOffset = normalOffset;
    }

    public int getOutputMode() {
        return outputMode;
    }

    public void setOutputMode(int outputMode) {
        this.outputMode = outputMode;
    }

    public int getOutputDepthImageIndex() {
        return outputDepthImageIndex;
    }

    public void setOutputDepthImageIndex(int outputDepthImageIndex) {
        this.outputDepthImageIndex = outputDepthImageIndex;
    }
}
