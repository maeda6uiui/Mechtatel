package com.github.maeda6uiui.mechtatel.core.blur;

/**
 * Info for simple blur
 *
 * @author maeda6uiui
 */
public class SimpleBlurInfo {
    private int textureWidth;
    private int textureHeight;
    private int stride;

    public SimpleBlurInfo() {
        textureWidth = 1280;
        textureHeight = 720;
        stride = 1;
    }

    public int getTextureWidth() {
        return textureWidth;
    }

    public void setTextureWidth(int textureWidth) {
        this.textureWidth = textureWidth;
    }

    public int getTextureHeight() {
        return textureHeight;
    }

    public void setTextureHeight(int textureHeight) {
        this.textureHeight = textureHeight;
    }

    public int getStride() {
        return stride;
    }

    public void setStride(int stride) {
        this.stride = stride;
    }
}