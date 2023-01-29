package com.github.maeda6uiui.mechtatel.core.shadow;

import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Shadow info
 *
 * @author maeda6uiui
 */
public class ShadowInfo {
    public static final int PROJECTION_TYPE_ORTHOGRAPHIC = 0;
    public static final int PROJECTION_TYPE_PERSPECTIVE = 1;

    private Matrix4f lightView;
    private Matrix4f lightProj;
    private Vector3f lightDirection;
    private Vector3f attenuations;
    private int projectionType;

    public ShadowInfo(ParallelLight parallelLight) {
        lightView = new Matrix4f().lookAt(
                parallelLight.getPosition(),
                parallelLight.getCenter(),
                new Vector3f(0.0f, 1.0f, 0.0f));
        lightProj = new Matrix4f().ortho(
                parallelLight.getOrthoLeft(),
                parallelLight.getOrthoRight(),
                parallelLight.getOrthoBottom(),
                parallelLight.getOrthoTop(),
                parallelLight.getzNear(),
                parallelLight.getzFar());
        lightProj.m11(lightProj.m11() * (-1.0f));
        lightDirection = parallelLight.getDirection();
        attenuations = parallelLight.getAttenuations();
        projectionType = PROJECTION_TYPE_ORTHOGRAPHIC;
    }

    public ShadowInfo(Spotlight spotlight) {
        lightView = new Matrix4f().lookAt(
                spotlight.getPosition(),
                spotlight.getCenter(),
                new Vector3f(0.0f, 1.0f, 0.0f));
        lightProj = new Matrix4f().perspective(
                spotlight.getFovY(),
                spotlight.getAspect(),
                spotlight.getzNear(),
                spotlight.getzFar());
        lightProj.m11(lightProj.m11() * (-1.0f));
        lightDirection = spotlight.getDirection();
        attenuations = spotlight.getAttenuations();
        projectionType = PROJECTION_TYPE_PERSPECTIVE;
    }

    public Matrix4f getLightView() {
        return lightView;
    }

    public void setLightView(Matrix4f lightView) {
        this.lightView = lightView;
    }

    public Matrix4f getLightProj() {
        return lightProj;
    }

    public void setLightProj(Matrix4f lightProj) {
        this.lightProj = lightProj;
    }

    public Vector3f getLightDirection() {
        return lightDirection;
    }

    public void setLightDirection(Vector3f lightDirection) {
        this.lightDirection = lightDirection;
    }

    public Vector3f getAttenuations() {
        return attenuations;
    }

    public void setAttenuations(Vector3f attenuations) {
        this.attenuations = attenuations;
    }

    public int getProjectionType() {
        return projectionType;
    }

    public void setProjectionType(int projectionType) {
        this.projectionType = projectionType;
    }
}
