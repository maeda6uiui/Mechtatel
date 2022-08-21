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
    private float rotateDelta;

    private final float MIN_ROTATE_V = (float) Math.toRadians(-80.0f);
    private final float MAX_ROTATE_V = (float) Math.toRadians(80.0f);

    private float rotateH;
    private float rotateV;

    public FreeCamera(Camera camera) {
        this.camera = camera;

        translateDelta = 0.1f;
        rotateDelta = 0.001f;

        rotateH = this.calcRotateH();
        rotateV = this.calcRotateV();
    }

    private float calcRotateH() {
        Vector3f eye = this.camera.eye;
        Vector3f center = this.camera.center;
        var front = new Vector3f(center).sub(eye).normalize();

        float th = new Vector3f(front.x, 0.0f, front.z).angle(new Vector3f(1.0f, 0.0f, 0.0f));
        if (front.z < 0.0f) {
            th *= (-1.0f);
        }

        return th;
    }

    private float calcRotateV() {
        Vector3f eye = this.camera.eye;
        Vector3f center = this.camera.center;
        var front = new Vector3f(center).sub(eye).normalize();

        float sinTh = front.y / front.length();
        float th = (float) Math.asin(sinTh);

        return th;
    }

    public void setTranslateDelta(float translateDelta) {
        this.translateDelta = translateDelta;
    }

    public void setRotateDelta(float rotateDelta) {
        this.rotateDelta = rotateDelta;
    }

    public float getTranslateDelta() {
        return translateDelta;
    }

    public float getRotateDelta() {
        return rotateDelta;
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

    public void rotate(int keyTop, int keyBottom, int keyLeft, int keyRight) {
        rotateH += keyRight * rotateDelta;
        rotateH -= keyLeft * rotateDelta;
        if (rotateH > Math.PI) {
            rotateH -= (float) Math.PI * 2.0f;
        } else if (rotateH < -Math.PI) {
            rotateH += (float) Math.PI * 2.0f;
        }

        rotateV += keyTop * rotateDelta;
        rotateV -= keyBottom * rotateDelta;
        if (rotateV < MIN_ROTATE_V) {
            rotateV = MIN_ROTATE_V;
        } else if (rotateV > MAX_ROTATE_V) {
            rotateV = MAX_ROTATE_V;
        }

        var lookAt = new Vector3f();
        lookAt.x = (float) Math.cos(rotateH);
        lookAt.y = (float) Math.sin(rotateV);
        lookAt.z = (float) Math.sin(rotateH);
        lookAt.normalize();

        var newCenter = new Vector3f(this.camera.eye).add(lookAt);
        this.camera.setCenter(newCenter);
    }
}
