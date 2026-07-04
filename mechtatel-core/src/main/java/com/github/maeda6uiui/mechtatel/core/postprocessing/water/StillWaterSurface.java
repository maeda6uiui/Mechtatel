package com.github.maeda6uiui.mechtatel.core.postprocessing.water;

import org.joml.Vector3f;

/**
 * Still water surface
 *
 * @author maeda6uiui
 */
public class StillWaterSurface {
    private float waterLevel;
    private Vector3f shallowColor;
    private Vector3f deepColor;
    private float distortionStrength;
    private float waveAmplitude;
    private float waveFrequency;
    private float waveSpeed;
    private float absorptionCoefficient;
    private float specularStrength;
    private Vector3f sunDirection;
    private Vector3f horizonColor;
    private Vector3f zenithColor;

    public StillWaterSurface() {
        waterLevel = 0.5f;
        shallowColor = new Vector3f(0.3f, 0.7f, 0.75f);
        deepColor = new Vector3f(0.0f, 0.1f, 0.25f);
        distortionStrength = 0.01f;
        waveAmplitude = 0.15f;
        waveFrequency = 1.2f;
        waveSpeed = 1.5f;
        absorptionCoefficient = 0.2f;
        specularStrength = 0.8f;
        sunDirection = new Vector3f(-0.4f, -1.0f, -0.3f).normalize();
        horizonColor = new Vector3f(0.7f, 0.8f, 0.85f);
        zenithColor = new Vector3f(0.25f, 0.45f, 0.75f);
    }

    public float getWaterLevel() {
        return waterLevel;
    }

    public void setWaterLevel(float waterLevel) {
        this.waterLevel = waterLevel;
    }

    public Vector3f getShallowColor() {
        return shallowColor;
    }

    public void setShallowColor(Vector3f shallowColor) {
        this.shallowColor = shallowColor;
    }

    public Vector3f getDeepColor() {
        return deepColor;
    }

    public void setDeepColor(Vector3f deepColor) {
        this.deepColor = deepColor;
    }

    public float getDistortionStrength() {
        return distortionStrength;
    }

    public void setDistortionStrength(float distortionStrength) {
        this.distortionStrength = distortionStrength;
    }

    public float getWaveAmplitude() {
        return waveAmplitude;
    }

    public void setWaveAmplitude(float waveAmplitude) {
        this.waveAmplitude = waveAmplitude;
    }

    public float getWaveFrequency() {
        return waveFrequency;
    }

    public void setWaveFrequency(float waveFrequency) {
        this.waveFrequency = waveFrequency;
    }

    public float getWaveSpeed() {
        return waveSpeed;
    }

    public void setWaveSpeed(float waveSpeed) {
        this.waveSpeed = waveSpeed;
    }

    public float getAbsorptionCoefficient() {
        return absorptionCoefficient;
    }

    public void setAbsorptionCoefficient(float absorptionCoefficient) {
        this.absorptionCoefficient = absorptionCoefficient;
    }

    public float getSpecularStrength() {
        return specularStrength;
    }

    public void setSpecularStrength(float specularStrength) {
        this.specularStrength = specularStrength;
    }

    public Vector3f getSunDirection() {
        return sunDirection;
    }

    public void setSunDirection(Vector3f sunDirection) {
        this.sunDirection = sunDirection;
    }

    public Vector3f getHorizonColor() {
        return horizonColor;
    }

    public void setHorizonColor(Vector3f horizonColor) {
        this.horizonColor = horizonColor;
    }

    public Vector3f getZenithColor() {
        return zenithColor;
    }

    public void setZenithColor(Vector3f zenithColor) {
        this.zenithColor = zenithColor;
    }
}
