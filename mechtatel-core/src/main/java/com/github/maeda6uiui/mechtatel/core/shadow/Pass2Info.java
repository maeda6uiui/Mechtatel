package com.github.maeda6uiui.mechtatel.core.shadow;

import static com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings.OUTPUT_MODE_SHADOW_MAPPING;

/**
 * Pass 2 info
 *
 * @author maeda6uiui
 */
public class Pass2Info {
    private int numShadowMaps;
    private float biasCoefficient;
    private float maxBias;
    private float normalOffset;
    private int outputMode;
    private int outputDepthImageIndex;

    public Pass2Info() {
        numShadowMaps = 0;
        biasCoefficient = 0.0001f;
        maxBias = 0.001f;
        normalOffset = 0.005f;
        outputMode = OUTPUT_MODE_SHADOW_MAPPING;
        outputDepthImageIndex = 0;
    }

    public Pass2Info(ShadowMappingSettings settings) {
        numShadowMaps = 0;
        biasCoefficient = settings.getBiasCoefficient();
        maxBias = settings.getMaxBias();
        normalOffset = settings.getNormalOffset();
        outputMode = settings.getOutputMode();
        outputDepthImageIndex = settings.getOutputDepthImageIndex();
    }

    public int getNumShadowMaps() {
        return numShadowMaps;
    }

    public void setNumShadowMaps(int numShadowMaps) {
        this.numShadowMaps = numShadowMaps;
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
