package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import org.joml.Matrix4f;

/**
 * Uniform buffer object for a camera
 *
 * @author maeda
 */
public class CameraUBO {
    public static final int SIZEOF = 3 * 16 * Float.BYTES;

    public Matrix4f model;
    public Matrix4f view;
    public Matrix4f proj;

    public CameraUBO() {
        model = new Matrix4f();
        view = new Matrix4f();
        proj = new Matrix4f();
    }
}
