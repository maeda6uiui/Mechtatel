package com.github.maeda6uiui.mechtatel.core.postprocessing.water;

import org.joml.Vector3f;

/**
 * Water surface
 *
 * @author maeda6uiui
 */
public class WaterSurface {
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

    public WaterSurface() {
        waterLevel = 0.0f;
        shallowColor = new Vector3f(0.25f, 0.6f, 0.7f);
        deepColor = new Vector3f(0.0f, 0.1f, 0.2f);
        distortionStrength = 0.02f;
        waveAmplitude = 0.15f;
        waveFrequency = 0.6f;
        waveSpeed = 1.0f;
        absorptionCoefficient = 0.15f;
        specularStrength = 0.6f;
        sunDirection = new Vector3f(-0.4f, -1.0f, -0.3f).normalize();
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
}