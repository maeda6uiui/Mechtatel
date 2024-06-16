package com.github.maeda6uiui.mechtatel.core.postprocessing.blur;

import org.joml.Vector2i;

/**
 * Info for simple blur
 *
 * @author maeda6uiui
 */
public class SimpleBlurInfo {
    private Vector2i textureSize;
    private int blurSize;
    private int stride;

    public SimpleBlurInfo() {
        textureSize = new Vector2i(1280, 720);
        blurSize = 5;
        stride = 1;
    }

    public Vector2i getTextureSize() {
        return textureSize;
    }

    public void setTextureSize(Vector2i textureSize) {
        this.textureSize = textureSize;
    }

    public int getBlurSize() {
        return blurSize;
    }

    public void setBlurSize(int blurSize) {
        this.blurSize = blurSize;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }
}
