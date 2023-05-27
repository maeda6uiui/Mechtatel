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

    private float fovY;
    private float aspect;
    private float zNear;
    private float zFar;

    public Camera() {
        eye = new Vector3f(5.0f, 5.0f, 5.0f);
        center = new Vector3f(0.0f, 0.0f, 0.0f);
        up = new Vector3f(0.0f, 1.0f, 0.0f);

        fovY = (float) Math.toRadians(100.0);
        aspect = 1280.0f / 720.0f;
        zNear = 0.1f;
        zFar = 500.0f;
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

    public float getFovY() {
        return fovY;
    }

    public void setFovY(float fovY) {
        this.fovY = fovY;
    }

    public float getAspect() {
        return aspect;
    }

    public void setAspect(float aspect) {
        this.aspect = aspect;
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
}
