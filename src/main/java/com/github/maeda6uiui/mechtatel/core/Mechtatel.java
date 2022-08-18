package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.*;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;
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

    public void closeWindow() {
        instance.closeWindow();
    }

    public void createPostProcessingNabors(List<String> naborNames) {
        instance.createPostProcessingNabors(naborNames);
    }

    public int getKeyboardPressingCount(String key) {
        return instance.getKeyboardPressingCount(key);
    }

    public int getKeyboardReleasingCount(String key) {
        return instance.getKeyboardReleasingCount(key);
    }

    public int getMousePressingCount(String key) {
        return instance.getMousePressingCount(key);
    }

    public int getMouseReleasingCount(String key) {
        return instance.getMouseReleasingCount(key);
    }

    public int getCursorPosX() {
        return instance.getCursorPosX();
    }

    public int getCursorPosY() {
        return instance.getCursorPosY();
    }

    /**
     * Fixes the cursor to (0,0).
     */
    public void fixCursor() {
        instance.setCursorPos(0, 0);
        instance.setFixCursorFlag(true);
    }

    public void unfixCursor() {
        instance.setFixCursorFlag(false);
    }

    public int setCursorMode(String cursorMode) {
        return instance.setCursorMode(cursorMode);
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

    public Line3D createLine3D(Vertex3D v1, Vertex3D v2) {
        return instance.createLine3D(v1, v2);
    }

    public Line3D createLine3D(Vector3fc p1, Vector4fc color1, Vector3fc p2, Vector4fc color2) {
        var v1 = new Vertex3D(p1, color1);
        var v2 = new Vertex3D(p2, color2);
        return instance.createLine3D(v1, v2);
    }

    public Line3D createLine3D(Vector3fc p1, Vector3fc p2, Vector4fc color) {
        var v1 = new Vertex3D(p1, color);
        var v2 = new Vertex3D(p2, color);
        return instance.createLine3D(v1, v2);
    }

    public Line3DSet createLine3DSet() {
        return instance.createLine3DSet();
    }

    public Line3DSet createAxesLine3DSet() {
        Line3DSet axes = instance.createLine3DSet();

        axes.add(new Vector3f(-100.0f, 0.0f, 0.0f), new Vector3f(100.0f, 0.0f, 0.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, -100.0f, 0.0f), new Vector3f(0.0f, 100.0f, 0.0f), new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, 0.0f, -100.0f), new Vector3f(0.0f, 0.0f, 100.0f), new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
        axes.createBuffer();

        return axes;
    }

    public Line3DSet createPositiveAxesLine3DSet() {
        Line3DSet axes = instance.createLine3DSet();

        axes.add(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(100.0f, 0.0f, 0.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 100.0f, 0.0f), new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        axes.add(new Vector3f(0.0f, 0.0f, 0.0f), new Vector3f(0.0f, 0.0f, 100.0f), new Vector4f(0.0f, 0.0f, 1.0f, 1.0f));
        axes.createBuffer();

        return axes;
    }

    public Sphere3D createSphere3D(Vector3fc center, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return instance.createSphere3D(center, radius, numVDivs, numHDivs, color);
    }

    public Capsule3D createCapsule3D(Vector3fc p1, Vector3fc p2, float radius, int numVDivs, int numHDivs, Vector4fc color) {
        return instance.createCapsule3D(p1, p2, radius, numVDivs, numHDivs, color);
    }
}
