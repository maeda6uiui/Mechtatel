package com.github.maeda6uiui.mechtatel.core.shadow;

/**
 * Pass 2 info
 *
 * @author maeda
 */
public class Pass2Info {
    private int numShadowMaps;
    private float biasCoefficient;
    private float maxBias;
    private float normalOffset;

    public Pass2Info() {
        numShadowMaps = 0;
        biasCoefficient = 0.0001f;
        maxBias = 0.001f;
        normalOffset = 0.005f;
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
}
