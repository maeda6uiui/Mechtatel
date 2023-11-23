package com.github.maeda6uiui.mechtatel.core.postprocessing.light;

import org.joml.Vector3f;

/**
 * Point light
 *
 * @author maeda6uiui
 */
public class PointLight {
    private Vector3f position;
    private Vector3f diffuseColor;
    private Vector3f diffuseClampMin;
    private Vector3f diffuseClampMax;
    private float k0;
    private float k1;
    private float k2;

    public PointLight() {
        position = new Vector3f(0.0f, 5.0f, 0.0f);
        diffuseColor = new Vector3f(1.0f, 1.0f, 1.0f);
        diffuseClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        diffuseClampMax = new Vector3f(1.0f, 1.0f, 1.0f);
        k0 = 0.0f;
        k1 = 0.0f;
        k2 = 0.05f;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(Vector3f diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public Vector3f getDiffuseClampMin() {
        return diffuseClampMin;
    }

    public void setDiffuseClampMin(Vector3f diffuseClampMin) {
        this.diffuseClampMin = diffuseClampMin;
    }

    public Vector3f getDiffuseClampMax() {
        return diffuseClampMax;
    }

    public void setDiffuseClampMax(Vector3f diffuseClampMax) {
        this.diffuseClampMax = diffuseClampMax;
    }

    public float getK0() {
        return k0;
    }

    public void setK0(float k0) {
        this.k0 = k0;
    }

    public float getK1() {
        return k1;
    }

    public void setK1(float k1) {
        this.k1 = k1;
    }

    public float getK2() {
        return k2;
    }

    public void setK2(float k2) {
        this.k2 = k2;
    }
}
