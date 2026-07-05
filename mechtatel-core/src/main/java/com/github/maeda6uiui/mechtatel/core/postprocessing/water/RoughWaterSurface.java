package com.github.maeda6uiui.mechtatel.core.postprocessing.water;

import org.joml.Vector3f;

/**
 * Rough water surface
 *
 * <p>Unlike {@link StillWaterSurface}, this effect targets a heavily distorted
 * ocean-like surface where the scene underneath is barely visible. It does not
 * refract; instead the surface is dominated by deep-water color, sky reflection,
 * and foam on the wave crests.
 *
 * @author maeda6uiui
 */
public class RoughWaterSurface {
    private float waterLevel;
    private Vector3f deepColor;
    private Vector3f shallowColor;
    private float transmissionStrength;
    private float waveAmplitude;
    private float waveFrequency;
    private float waveSpeed;
    private float choppiness;
    private float swellAmplitude;
    private float swellFrequency;
    private float swellSpeed;
    private Vector3f foamColor;
    private float foamThreshold;
    private float foamIntensity;
    private float specularStrength;
    private Vector3f sunDirection;
    private Vector3f horizonColor;
    private Vector3f zenithColor;

    public RoughWaterSurface() {
        waterLevel = 0.5f;
        deepColor = new Vector3f(0.0f, 0.08f, 0.2f);
        shallowColor = new Vector3f(0.1f, 0.35f, 0.45f);
        transmissionStrength = 0.15f;
        waveAmplitude = 0.4f;
        waveFrequency = 1.0f;
        waveSpeed = 1.8f;
        choppiness = 2.0f;
        swellAmplitude = 0.5f;
        swellFrequency = 0.3f;
        swellSpeed = 0.7f;
        foamColor = new Vector3f(0.9f, 0.95f, 1.0f);
        foamThreshold = 0.6f;
        foamIntensity = 1.0f;
        specularStrength = 1.2f;
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

    public Vector3f getDeepColor() {
        return deepColor;
    }

    public void setDeepColor(Vector3f deepColor) {
        this.deepColor = deepColor;
    }

    public Vector3f getShallowColor() {
        return shallowColor;
    }

    public void setShallowColor(Vector3f shallowColor) {
        this.shallowColor = shallowColor;
    }

    public float getTransmissionStrength() {
        return transmissionStrength;
    }

    public void setTransmissionStrength(float transmissionStrength) {
        this.transmissionStrength = transmissionStrength;
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

    public float getChoppiness() {
        return choppiness;
    }

    public void setChoppiness(float choppiness) {
        this.choppiness = choppiness;
    }

    public float getSwellAmplitude() {
        return swellAmplitude;
    }

    public void setSwellAmplitude(float swellAmplitude) {
        this.swellAmplitude = swellAmplitude;
    }

    public float getSwellFrequency() {
        return swellFrequency;
    }

    public void setSwellFrequency(float swellFrequency) {
        this.swellFrequency = swellFrequency;
    }

    public float getSwellSpeed() {
        return swellSpeed;
    }

    public void setSwellSpeed(float swellSpeed) {
        this.swellSpeed = swellSpeed;
    }

    public Vector3f getFoamColor() {
        return foamColor;
    }

    public void setFoamColor(Vector3f foamColor) {
        this.foamColor = foamColor;
    }

    public float getFoamThreshold() {
        return foamThreshold;
    }

    public void setFoamThreshold(float foamThreshold) {
        this.foamThreshold = foamThreshold;
    }

    public float getFoamIntensity() {
        return foamIntensity;
    }

    public void setFoamIntensity(float foamIntensity) {
        this.foamIntensity = foamIntensity;
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
