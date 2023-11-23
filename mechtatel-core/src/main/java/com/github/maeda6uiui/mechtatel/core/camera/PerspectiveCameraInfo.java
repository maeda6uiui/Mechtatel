package com.github.maeda6uiui.mechtatel.core.camera;

/**
 * Info for perspective camera
 *
 * @author maeda6uiui
 */
public class PerspectiveCameraInfo {
    public float fovY;
    public float aspect;

    public PerspectiveCameraInfo() {
        fovY = (float) Math.toRadians(60.0);
        aspect = 1280.0f / 720.0f;
    }
}
