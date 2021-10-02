package com.github.maeda6uiui.mechtatel.core.light;

import org.joml.Vector3f;

/**
 * Spotlight
 *
 * @author maeda
 */
public class Spotlight {
    private Vector3f position;
    private Vector3f direction;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private Vector3f diffuseClampMin;
    private Vector3f diffuseClampMax;
    private Vector3f specularClampMin;
    private Vector3f specularClampMax;
    private float k0;
    private float k1;
    private float k2;
    private float theta; //inner corn
    private float phi; //outer corn
    private float falloff;
    private float specularPowY;

    public Spotlight() {
        position = new Vector3f(50.0f, 50.0f, 50.0f);
        direction = new Vector3f(-1.0f, -1.0f, -1.0f).normalize();
        diffuseColor = new Vector3f(1.0f, 1.0f, 1.0f);
        specularColor = new Vector3f(1.0f, 1.0f, 1.0f);
        diffuseClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        diffuseClampMax = new Vector3f(0.7f, 0.7f, 0.7f);
        specularClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        specularClampMax = new Vector3f(0.3f, 0.3f, 0.3f);
        k0 = 0.0f;
        k1 = 0.0f;
        k2 = 1.0f;
        theta = (float) Math.toRadians(20);
        phi = (float) Math.toRadians(50);
        falloff = 1.0f;
        specularPowY = 2.0f;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction.normalize();
    }

    public Vector3f getDiffuseColor() {
        return diffuseColor;
    }

    public void setDiffuseColor(Vector3f diffuseColor) {
        this.diffuseColor = diffuseColor;
    }

    public Vector3f getSpecularColor() {
        return specularColor;
    }

    public void setSpecularColor(Vector3f specularColor) {
        this.specularColor = specularColor;
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

    public Vector3f getSpecularClampMin() {
        return specularClampMin;
    }

    public void setSpecularClampMin(Vector3f specularClampMin) {
        this.specularClampMin = specularClampMin;
    }

    public Vector3f getSpecularClampMax() {
        return specularClampMax;
    }

    public void setSpecularClampMax(Vector3f specularClampMax) {
        this.specularClampMax = specularClampMax;
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

    public float getTheta() {
        return theta;
    }

    public void setTheta(float theta) {
        this.theta = theta;
    }

    public float getPhi() {
        return phi;
    }

    public void setPhi(float phi) {
        this.phi = phi;
    }

    public float getFalloff() {
        return falloff;
    }

    public void setFalloff(float falloff) {
        this.falloff = falloff;
    }

    public float getSpecularPowY() {
        return specularPowY;
    }

    public void setSpecularPowY(float specularPowY) {
        this.specularPowY = specularPowY;
    }
}
