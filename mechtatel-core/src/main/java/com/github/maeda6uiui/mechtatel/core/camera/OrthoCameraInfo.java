package com.github.maeda6uiui.mechtatel.core.camera;

/**
 * Info for orthographic camera
 *
 * @author maeda6uiui
 */
public class OrthoCameraInfo {
    public float left;
    public float right;
    public float bottom;
    public float top;

    public OrthoCameraInfo() {
        left = -10.0f;
        right = 10.0f;
        bottom = -10.0f;
        top = 10.0f;
    }
}
