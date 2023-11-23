package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.camera.CameraMode;
import com.github.maeda6uiui.mechtatel.core.camera.OrthoCameraInfo;
import com.github.maeda6uiui.mechtatel.core.camera.PerspectiveCameraInfo;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.nio.ByteBuffer;

import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_MAT4;
import static com.github.maeda6uiui.mechtatel.core.vulkan.ubo.SizeofInfo.SIZEOF_VEC4;

/**
 * Uniform buffer object for a camera
 *
 * @author maeda6uiui
 */
public class CameraUBO extends UBO {
    public static final int SIZEOF = SIZEOF_MAT4 * 2 + SIZEOF_VEC4 * 2;

    private Matrix4f view;
    private Matrix4f proj;

    private Vector3f eye;
    private Vector3f center;

    public CameraUBO(Camera camera) {
        Vector3fc eye = camera.getEye();
        Vector3fc center = camera.getCenter();
        Vector3fc up = camera.getUp();
        float zNear = camera.getZNear();
        float zFar = camera.getZFar();

        view = new Matrix4f().lookAt(
                eye.x(), eye.y(), eye.z(),
                center.x(), center.y(), center.z(),
                up.x(), up.y(), up.z());

        if (camera.getCameraMode() == CameraMode.ORTHOGRAPHIC) {
            OrthoCameraInfo cameraInfo = camera.getOrthoCameraInfo();
            proj = new Matrix4f()
                    .scale(1.0f, -1.0f, 1.0f)
                    .ortho(
                            cameraInfo.left,
                            cameraInfo.right,
                            cameraInfo.bottom,
                            cameraInfo.top,
                            zNear,
                            zFar,
                            true);
        } else {
            PerspectiveCameraInfo cameraInfo = camera.getPerspectiveCameraInfo();
            proj = new Matrix4f()
                    .scale(1.0f, -1.0f, 1.0f)
                    .perspective(cameraInfo.fovY, cameraInfo.aspect, zNear, zFar, true);
        }

        this.eye = new Vector3f(eye);
        this.center = new Vector3f(center);
    }

    @Override
    protected void memcpy(ByteBuffer buffer) {
        view.get(0, buffer);
        proj.get(SIZEOF_MAT4, buffer);
        eye.get(SIZEOF_MAT4 * 2, buffer);
        center.get(SIZEOF_MAT4 * 2 + SIZEOF_VEC4, buffer);

        buffer.rewind();
    }

    @Override
    protected int getSize() {
        return SIZEOF;
    }
}
