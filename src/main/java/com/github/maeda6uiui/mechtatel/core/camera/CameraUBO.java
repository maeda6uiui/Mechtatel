package com.github.maeda6uiui.mechtatel.core.camera;

import org.joml.Matrix4f;

/**
 * Uniform buffer object for a camera
 *
 * @author maeda
 */
public class CameraUBO {
    public static final int SIZEOF = 2 * 16 * Float.BYTES;

    public Matrix4f view;
    public Matrix4f proj;

    public CameraUBO() {
        view = new Matrix4f();
        proj = new Matrix4f();
    }
}
