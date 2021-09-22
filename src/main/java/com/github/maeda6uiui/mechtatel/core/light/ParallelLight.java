package com.github.maeda6uiui.mechtatel.core.light;

import org.joml.Vector3f;

/**
 * Parallel light
 *
 * @author maeda
 */
public class ParallelLight {
    private Vector3f direction;
    private Vector3f ambientColor;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private Vector3f ambientClampMin;
    private Vector3f ambientClampMax;
    private Vector3f diffuseClampMin;
    private Vector3f diffuseClampMax;
    private Vector3f specularClampMin;
    private Vector3f specularClampMax;
    private float speculatPowY;

    public ParallelLight() {
        direction = new Vector3f(-1.0f, -1.0f, -1.0f).normalize();
        ambientColor = new Vector3f(1.0f, 1.0f, 1.0f);
        diffuseColor = new Vector3f(1.0f, 1.0f, 1.0f);
        specularColor = new Vector3f(1.0f, 1.0f, 1.0f);
        ambientClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        ambientClampMax = new Vector3f(0.6f, 0.6f, 0.6f);
        diffuseClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        diffuseClampMax = new Vector3f(0.2f, 0.2f, 0.2f);
        specularClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        specularClampMax = new Vector3f(0.2f, 0.2f, 0.2f);
        speculatPowY = 2.0f;
    }

    public Vector3f getDirection() {
        return direction;
    }

    public void setDirection(Vector3f direction) {
        this.direction = direction;
    }

    public Vector3f getAmbientColor() {
        return ambientColor;
    }

    public void setAmbientColor(Vector3f ambientColor) {
        this.ambientColor = ambientColor;
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

    public Vector3f getAmbientClampMin() {
        return ambientClampMin;
    }

    public void setAmbientClampMin(Vector3f ambientClampMin) {
        this.ambientClampMin = ambientClampMin;
    }

    public Vector3f getAmbientClampMax() {
        return ambientClampMax;
    }

    public void setAmbientClampMax(Vector3f ambientClampMax) {
        this.ambientClampMax = ambientClampMax;
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

    public float getSpeculatPowY() {
        return speculatPowY;
    }

    public void setSpeculatPowY(float speculatPowY) {
        this.speculatPowY = speculatPowY;
    }
}
