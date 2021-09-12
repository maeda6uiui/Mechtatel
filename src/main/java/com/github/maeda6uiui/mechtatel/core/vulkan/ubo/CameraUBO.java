package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import org.joml.Matrix4f;
import org.joml.Vector3fc;

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

    public CameraUBO(Camera camera) {
        Vector3fc eye = camera.getEye();
        Vector3fc center = camera.getCenter();
        Vector3fc up = camera.getUp();
        float fovY = camera.getFovY();
        float aspect = camera.getAspect();
        float zNear = camera.getZNear();
        float zFar = camera.getZFar();

        view = new Matrix4f().lookAt(
                eye.x(), eye.y(), eye.z(),
                center.x(), center.y(), center.z(),
                up.x(), up.y(), up.z());
        proj = new Matrix4f().perspective(fovY, aspect, zNear, zFar);
        proj.m11(proj.m11() * (-1.0f));
    }
}
