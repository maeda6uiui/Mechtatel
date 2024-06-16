package com.github.maeda6uiui.mechtatel.core.fseffect;

import org.joml.Vector2i;

import java.util.Arrays;

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
     */
    public GaussianBlurInfo(int numWeights, float standardDeviation) {
        textureSize = new Vector2i(1280, 720);

        weights = new float[numWeights];
        Arrays.fill(weights, 0.0f);

        float s2 = standardDeviation * standardDeviation;
        float sum = 0.0f;
        for (int i = 0; i < numWeights; i++) {
            float x2 = i * i;
            float weight = (float) Math.exp(-0.5f * x2 / s2);
            weights[i] = weight;

            if (i == 0) {
                sum += weight * 2.0f;
            } else {
                sum += weight * 4.0f;
            }
        }
        for (int i = 0; i < numWeights; i++) {
            weights[i] /= sum;
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
