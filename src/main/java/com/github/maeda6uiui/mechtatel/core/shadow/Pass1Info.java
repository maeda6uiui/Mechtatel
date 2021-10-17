package com.github.maeda6uiui.mechtatel.core.shadow;

import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Pass 1 info
 *
 * @author maeda
 */
public class Pass1Info {
    private Matrix4f view;
    private Matrix4f proj;
    private float normalOffset;

    public Pass1Info() {
        view = new Matrix4f();
        proj = new Matrix4f();
        normalOffset = 0.05f;
    }

    public Pass1Info(ParallelLight light) {
        view = new Matrix4f().lookAlong(light.getDirection(), new Vector3f(0.0f, 1.0f, 0.0f));
        proj = new Matrix4f().ortho(
                light.getOrthoLeft(),
                light.getOrthoRight(),
                light.getOrthoBottom(),
                light.getOrthoTop(),
                light.getzNear(),
                light.getzFar());
        normalOffset = 0.05f;
    }

    public Pass1Info(Spotlight light) {
        view = new Matrix4f().lookAlong(light.getDirection(), new Vector3f(0.0f, 1.0f, 0.0f));
        proj = new Matrix4f().perspective(light.getFovY(), light.getAspect(), light.getzNear(), light.getzFar());
        normalOffset = 0.05f;
    }

    public Matrix4f getView() {
        return view;
    }

    public void setView(Matrix4f view) {
        this.view = view;
    }

    public Matrix4f getProj() {
        return proj;
    }

    public void setProj(Matrix4f proj) {
        this.proj = proj;
    }

    public float getNormalOffset() {
        return normalOffset;
    }

    public void setNormalOffset(float normalOffset) {
        this.normalOffset = normalOffset;
    }
}
