package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Base class of the Mechtatel engine
 *
 * @author maeda
 */
public class Mechtatel implements IMechtatel {
    private final Logger logger = LoggerFactory.getLogger(Mechtatel.class);

    private MttInstance instance;

    public Mechtatel(MttSettings settings) {
        logger.info(settings.toString());

        try {
            instance = new MttInstance(this, settings);
            logger.info("MttInstance successfully created");

            logger.info("Start running the main loop");
            instance.run();

            logger.info("Start cleaning up Mechtatel");
            instance.cleanup();
        } catch (Exception e) {
            logger.error("Fatal error", e);
            return;
        }
    }

    @Override
    public void init() {
        logger.info("Mechtatel initialized");
    }

    @Override
    public void dispose() {
        logger.info("Mechtatel disposed");
    }

    @Override
    public void reshape(int width, int height) {
        logger.trace("Framebuffer size changed: (width, height)=({},{})", width, height);
    }

    @Override
    public void update() {

    }

    public void createPostProcessingNabors(List<String> naborNames) {
        instance.createPostProcessingNabors(naborNames);
    }

    public Vector4f getBackgroundColor() {
        return instance.getBackgroundColor();
    }

    public void setBackgroundColor(Vector4f backgroundColor) {
        instance.setBackgroundColor(backgroundColor);
    }

    public Vector3f getParallelLightAmbientColor() {
        return instance.getParallelLightAmbientColor();
    }

    public void setParallelLightAmbientColor(Vector3f parallelLightAmbientColor) {
        instance.setParallelLightAmbientColor(parallelLightAmbientColor);
    }

    public Vector3f getPointLightAmbientColor() {
        return instance.getPointLightAmbientColor();
    }

    public void setPointLightAmbientColor(Vector3f pointLightAmbientColor) {
        instance.setPointLightAmbientColor(pointLightAmbientColor);
    }

    public Vector3f getSpotlightAmbientColor() {
        return instance.getSpotlightAmbientColor();
    }

    public void setSpotlightAmbientColor(Vector3f spotlightAmbientColor) {
        instance.setSpotlightAmbientColor(spotlightAmbientColor);
    }

    public ShadowMappingSettings getShadowMappingSettings() {
        return instance.getShadowMappingSettings();
    }

    public void setShadowMappingSettings(ShadowMappingSettings shadowMappingSettings) {
        instance.setShadowMappingSettings(shadowMappingSettings);
    }

    public Camera getCamera() {
        return instance.getCamera();
    }

    public Fog getFog() {
        return instance.getFog();
    }

    public int getNumParallelLights() {
        return instance.getNumParallelLights();
    }

    public ParallelLight getParallelLight(int index) {
        return instance.getParallelLight(index);
    }

    public ParallelLight createParallelLight() {
        return instance.createParallelLight();
    }

    public boolean removeParallelLight(ParallelLight parallelLight) {
        return instance.removeParallelLight(parallelLight);
    }

    public int getNumPointLights() {
        return instance.getNumPointLights();
    }

    public PointLight getPointLight(int index) {
        return instance.getPointLight(index);
    }

    public PointLight createPointLight() {
        return instance.createPointLight();
    }

    public boolean removePointLight(PointLight pointLight) {
        return instance.removePointLight(pointLight);
    }

    public int getNumSpotlights() {
        return instance.getNumSpotlights();
    }

    public Spotlight getSpotlight(int index) {
        return instance.getSpotlight(index);
    }

    public Spotlight createSpotlight() {
        return instance.createSpotlight();
    }

    public boolean removeSpotlight(Spotlight spotlight) {
        return instance.removeSpotlight(spotlight);
    }

    //=== Methods relating to components ===
    public Model3D createModel3D(String modelFilepath) {
        return instance.createModel3D(modelFilepath);
    }

    public Model3D duplicateModel3D(Model3D srcModel) {
        return instance.duplicateModel3D(srcModel);
    }
}
