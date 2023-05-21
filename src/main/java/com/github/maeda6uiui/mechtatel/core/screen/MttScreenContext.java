package com.github.maeda6uiui.mechtatel.core.screen;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

/**
 * Context storing properties for a screen
 *
 * @author maeda6uiui
 */
public class MttScreenContext {
    private Vector4f backgroundColor;
    private Camera camera;
    private Fog fog;
    private List<ParallelLight> parallelLights;
    private Vector3f parallelLightAmbientColor;
    private List<PointLight> pointLights;
    private Vector3f pointLightAmbientColor;
    private List<Spotlight> spotlights;
    private Vector3f spotlightAmbientColor;
    private ShadowMappingSettings shadowMappingSettings;

    public MttScreenContext(
            String screenName,
            int depthImageWidth,
            int depthImageHeight,
            int screenWidth,
            int screenHeight,
            boolean shouldChangeExtentOnRecreate) {
        backgroundColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        parallelLightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        pointLightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        spotlightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        shadowMappingSettings = new ShadowMappingSettings();

        camera = new Camera();
        fog = new Fog();
        parallelLights = new ArrayList<>();
        pointLights = new ArrayList<>();
        spotlights = new ArrayList<>();
    }

    public Vector4f getBackgroundColor() {
        return backgroundColor;
    }

    public void setBackgroundColor(Vector4f backgroundColor) {
        this.backgroundColor = backgroundColor;
    }

    public Camera getCamera() {
        return camera;
    }

    public void setCamera(Camera camera) {
        this.camera = camera;
    }

    public Fog getFog() {
        return fog;
    }

    public void setFog(Fog fog) {
        this.fog = fog;
    }

    public List<ParallelLight> getParallelLights() {
        return parallelLights;
    }

    public void setParallelLights(List<ParallelLight> parallelLights) {
        this.parallelLights = parallelLights;
    }

    public Vector3f getParallelLightAmbientColor() {
        return parallelLightAmbientColor;
    }

    public void setParallelLightAmbientColor(Vector3f parallelLightAmbientColor) {
        this.parallelLightAmbientColor = parallelLightAmbientColor;
    }

    public List<PointLight> getPointLights() {
        return pointLights;
    }

    public void setPointLights(List<PointLight> pointLights) {
        this.pointLights = pointLights;
    }

    public Vector3f getPointLightAmbientColor() {
        return pointLightAmbientColor;
    }

    public void setPointLightAmbientColor(Vector3f pointLightAmbientColor) {
        this.pointLightAmbientColor = pointLightAmbientColor;
    }

    public List<Spotlight> getSpotlights() {
        return spotlights;
    }

    public void setSpotlights(List<Spotlight> spotlights) {
        this.spotlights = spotlights;
    }

    public Vector3f getSpotlightAmbientColor() {
        return spotlightAmbientColor;
    }

    public void setSpotlightAmbientColor(Vector3f spotlightAmbientColor) {
        this.spotlightAmbientColor = spotlightAmbientColor;
    }

    public ShadowMappingSettings getShadowMappingSettings() {
        return shadowMappingSettings;
    }

    public void setShadowMappingSettings(ShadowMappingSettings shadowMappingSettings) {
        this.shadowMappingSettings = shadowMappingSettings;
    }
}
