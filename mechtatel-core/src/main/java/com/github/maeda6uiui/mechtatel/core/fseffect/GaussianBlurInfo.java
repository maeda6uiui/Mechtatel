package com.github.maeda6uiui.mechtatel.core.fseffect;

import org.joml.Vector2i;

/**
 * Info for Gaussian blur
 *
 * @author maeda6uiui
 */
public class GaussianBlurInfo {
    private Vector2i textureSize;
    private float[] weights;

    /**
     * Creates Gaussian blur info.
     * Weight array may be truncated if its length exceeds the maximum allowed by the implementation.
     *
     * @param numWeights        Number of weights
     * @param standardDeviation Standard deviation
     * @param amplitude         Amplitude
     */
    public GaussianBlurInfo(int numWeights, float standardDeviation, float amplitude) {
        textureSize = new Vector2i(1280, 720);

        weights = new float[numWeights];

        float s2 = standardDeviation * standardDeviation;
        for (int i = 0; i < numWeights; i++) {
            float x2 = i * i;
            float weight = (float) Math.exp(-0.5f * x2 / s2);

            weights[i] = weight * amplitude;
        }
    }

    public Vector2i getTextureSize() {
        return textureSize;
    }

    public void setTextureSize(Vector2i textureSize) {
        this.textureSize = textureSize;
    }

    public float[] getWeights() {
        return weights;
    }
}
