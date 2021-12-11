package com.github.maeda6uiui.mechtatel.core.light;

import org.joml.Vector3f;

/**
 * Parallel light
 *
 * @author maeda
 */
public class ParallelLight {
    private Vector3f direction;
    private Vector3f diffuseColor;
    private Vector3f specularColor;
    private Vector3f diffuseClampMin;
    private Vector3f diffuseClampMax;
    private Vector3f specularClampMin;
    private Vector3f specularClampMax;
    private float specularPowY;

    private boolean castShadow;
    private Vector3f position;
    private Vector3f center;
    private float orthoLeft;
    private float orthoRight;
    private float orthoBottom;
    private float orthoTop;
    private float zNear;
    private float zFar;
    private Vector3f attenuations;

    public ParallelLight() {
        direction = new Vector3f(-1.0f, -1.0f, -1.0f).normalize();
        diffuseColor = new Vector3f(1.0f, 1.0f, 1.0f);
        specularColor = new Vector3f(1.0f, 1.0f, 1.0f);
        diffuseClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        diffuseClampMax = new Vector3f(0.3f, 0.3f, 0.3f);
        specularClampMin = new Vector3f(0.0f, 0.0f, 0.0f);
        specularClampMax = new Vector3f(0.2f, 0.2f, 0.2f);
        specularPowY = 2.0f;

        castShadow = true;
        position = new Vector3f(50.0f, 50.0f, 50.0f);
        center = new Vector3f(0.0f, 0.0f, 0.0f);
        orthoLeft = -10.0f;
        orthoRight = 10.0f;
        orthoBottom = -10.0f;
        orthoTop = 10.0f;
        zNear = 0.1f;
        zFar = 100.0f;
        attenuations = new Vector3f(0.5f, 0.5f, 0.5f);
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

    public float getSpecularPowY() {
        return specularPowY;
    }

    public void setSpecularPowY(float specularPowY) {
        this.specularPowY = specularPowY;
    }

    public boolean isCastShadow() {
        return castShadow;
    }

    public void setCastShadow(boolean castShadow) {
        this.castShadow = castShadow;
    }

    public Vector3f getPosition() {
        return position;
    }

    public void setPosition(Vector3f position) {
        this.position = position;
    }

    public Vector3f getCenter() {
        return center;
    }

    public void setCenter(Vector3f center) {
        this.center = center;
    }

    public float getOrthoLeft() {
        return orthoLeft;
    }

    public void setOrthoLeft(float orthoLeft) {
        this.orthoLeft = orthoLeft;
    }

    public float getOrthoRight() {
        return orthoRight;
    }

    public void setOrthoRight(float orthoRight) {
        this.orthoRight = orthoRight;
    }

    public float getOrthoBottom() {
        return orthoBottom;
    }

    public void setOrthoBottom(float orthoBottom) {
        this.orthoBottom = orthoBottom;
    }

    public float getOrthoTop() {
        return orthoTop;
    }

    public void setOrthoTop(float orthoTop) {
        this.orthoTop = orthoTop;
    }

    public float getzNear() {
        return zNear;
    }

    public void setzNear(float zNear) {
        this.zNear = zNear;
    }

    public float getzFar() {
        return zFar;
    }

    public void setzFar(float zFar) {
        this.zFar = zFar;
    }

    public Vector3f getAttenuations() {
        return attenuations;
    }

    public void setAttenuations(Vector3f attenuations) {
        this.attenuations = attenuations;
    }
}
