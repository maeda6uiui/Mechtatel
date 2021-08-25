package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Matrix4f;
import org.joml.Matrix4fc;

/**
 * Component
 *
 * @author maeda
 */
public class Component {
    private static int count;

    static {
        count = 0;
    }

    private String tag;
    private Matrix4f mat;
    private boolean visible;

    public Component() {
        tag = "Component_" + count;
        mat = new Matrix4f().identity();
        visible = true;

        count++;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public Matrix4fc getMat() {
        return mat;
    }

    protected Matrix4f getMatRef() {
        return mat;
    }

    public void setMat(Matrix4fc mat) {
        this.mat = new Matrix4f(mat);
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public boolean getVisible() {
        return visible;
    }

    public void applyMat(Matrix4fc right) {
        this.mat.mul(right);
    }

    public void reset() {
        this.mat.invert();
    }

    public void draw() {

    }
}
