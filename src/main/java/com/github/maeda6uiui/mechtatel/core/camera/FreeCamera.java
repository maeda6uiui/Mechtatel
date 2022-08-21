package com.github.maeda6uiui.mechtatel.core.camera;

import org.joml.Vector3f;

/**
 * Camera that moves according to user input
 *
 * @author maeda
 */
public class FreeCamera {
    private Camera camera;
    private float translateDelta;

    public FreeCamera(Camera camera) {
        this.camera = camera;

        translateDelta = 0.1f;
    }

    public void setTranslateDelta(float translateDelta) {
        this.translateDelta = translateDelta;
    }

    public float getTranslateDelta() {
        return translateDelta;
    }

    public void translate(int keyFront, int keyBack, int keyLeft, int keyRight) {
        Vector3f eye = this.camera.eye;
        Vector3f center = this.camera.center;

        var front = new Vector3f(center).sub(eye).normalize();
        var left = new Vector3f(0.0f, 1.0f, 0.0f).cross(front);

        var translateVec = new Vector3f();
        if (keyFront > 0) {
            translateVec.add(front);
        }
        if (keyBack > 0) {
            translateVec.add(front.mul(-1.0f));
        }
        if (keyLeft > 0) {
            translateVec.add(left);
        }
        if (keyRight > 0) {
            translateVec.add(left.mul(-1.0f));
        }
        if (translateVec.length() > 0.001f) {
            translateVec.normalize().mul(translateDelta);
        }

        var newEye = new Vector3f(eye).add(translateVec);
        var newCenter = new Vector3f(center).add(translateVec);
        this.camera.setEye(newEye);
        this.camera.setCenter(newCenter);
    }
}
