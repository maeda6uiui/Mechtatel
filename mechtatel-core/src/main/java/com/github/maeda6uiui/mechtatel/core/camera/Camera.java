package com.github.maeda6uiui.mechtatel.core.camera;

import org.joml.Vector3f;

/**
 * Camera
 *
 * @author maeda6uiui
 */
public class Camera {
    private Vector3f eye;
    private Vector3f center;
    private Vector3f up;
    private float zNear;
    private float zFar;

    private CameraMode cameraMode;
    private OrthoCameraInfo orthoCameraInfo;
    private PerspectiveCameraInfo perspectiveCameraInfo;

    public Camera() {
        eye = new Vector3f(5.0f, 5.0f, 5.0f);
        center = new Vector3f(0.0f, 0.0f, 0.0f);
        up = new Vector3f(0.0f, 1.0f, 0.0f);
        zNear = 0.1f;
        zFar = 500.0f;

        cameraMode = CameraMode.PERSPECTIVE;
        orthoCameraInfo = new OrthoCameraInfo();
        perspectiveCameraInfo = new PerspectiveCameraInfo();
    }

    public Vector3f getEye() {
        return eye;
    }

    public void setEye(Vector3f eye) {
        this.eye = eye;
    }

    public Vector3f getCenter() {
        return center;
    }

    public void setCenter(Vector3f center) {
        this.center = center;
    }

    public Vector3f getUp() {
        return up;
    }

    public void setUp(Vector3f up) {
        this.up = up;
    }

    public float getZNear() {
        return zNear;
    }

    public void setZNear(float zNear) {
        this.zNear = zNear;
    }

    public float getZFar() {
        return zFar;
    }

    public void setZFar(float zFar) {
        this.zFar = zFar;
    }

    public CameraMode getCameraMode() {
        return cameraMode;
    }

    public void setCameraMode(CameraMode cameraMode) {
        this.cameraMode = cameraMode;
    }

    public OrthoCameraInfo getOrthoCameraInfo() {
        return orthoCameraInfo;
    }

    public void setOrthoCameraInfo(OrthoCameraInfo orthoCameraInfo) {
        this.orthoCameraInfo = orthoCameraInfo;
    }

    public PerspectiveCameraInfo getPerspectiveCameraInfo() {
        return perspectiveCameraInfo;
    }

    public void setPerspectiveCameraInfo(PerspectiveCameraInfo perspectiveCameraInfo) {
        this.perspectiveCameraInfo = perspectiveCameraInfo;
    }
}
