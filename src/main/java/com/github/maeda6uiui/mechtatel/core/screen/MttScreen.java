package com.github.maeda6uiui.mechtatel.core.screen;

import com.github.maeda6uiui.mechtatel.core.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.fog.Fog;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
import com.github.maeda6uiui.mechtatel.core.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import com.github.maeda6uiui.mechtatel.core.vulkan.IMttVulkanInstanceForScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.FlexibleNaborInfo;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.ParallelLightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.PointLightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.nabor.postprocessing.SpotlightNabor;
import com.github.maeda6uiui.mechtatel.core.vulkan.screen.VkMttScreen;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Screen
 *
 * @author maeda6uiui
 */
public class MttScreen {
    private VkMttScreen screen;

    private boolean shouldAutoUpdateCameraAspect;

    private IMttVulkanInstanceForScreen vulkanInstance;

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
    private SimpleBlurInfo simpleBlurInfo;

    public MttScreen(
            MttVulkanInstance vulkanInstance,
            String screenName,
            int depthImageWidth,
            int depthImageHeight,
            int screenWidth,
            int screenHeight,
            boolean shouldChangeExtentOnRecreate,
            String samplerFilter,
            String samplerMipmapMode,
            String samplerAddressMode,
            Map<String, ExternalPostProcessingNaborInfo> externalPostProcessingNaborInfos,
            List<String> ppNaborNames) {
        var flexibleNaborInfos = new HashMap<String, FlexibleNaborInfo>();

        for (var entry : externalPostProcessingNaborInfos.entrySet()) {
            String naborName = entry.getKey();
            ExternalPostProcessingNaborInfo naborInfo = entry.getValue();

            var flexibleNaborInfo = new FlexibleNaborInfo(
                    naborInfo.getVertShaderFilepath(), naborInfo.getFragShaderFilepath());
            flexibleNaborInfo.setUniformResources(naborInfo.getUniformResources());
            flexibleNaborInfos.put(naborName, flexibleNaborInfo);
        }

        screen = vulkanInstance.createScreen(
                screenName,
                depthImageWidth,
                depthImageHeight,
                screenWidth,
                screenHeight,
                shouldChangeExtentOnRecreate,
                samplerFilter,
                samplerMipmapMode,
                samplerAddressMode,
                flexibleNaborInfos,
                (ppNaborNames != null && ppNaborNames.size() != 0) ? ppNaborNames : null
        );
        shouldAutoUpdateCameraAspect = true;

        this.vulkanInstance = vulkanInstance;

        backgroundColor = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);
        parallelLightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        pointLightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        spotlightAmbientColor = new Vector3f(0.5f, 0.5f, 0.5f);
        shadowMappingSettings = new ShadowMappingSettings();
        simpleBlurInfo = new SimpleBlurInfo();

        camera = new Camera();
        fog = new Fog();
        parallelLights = new ArrayList<>();
        pointLights = new ArrayList<>();
        spotlights = new ArrayList<>();
    }

    public void cleanup() {
        screen.cleanup();
    }

    public void draw() {
        vulkanInstance.draw(
                screen,
                backgroundColor,
                camera,
                fog,
                parallelLights,
                parallelLightAmbientColor,
                pointLights,
                pointLightAmbientColor,
                spotlights,
                spotlightAmbientColor,
                shadowMappingSettings,
                simpleBlurInfo
        );
    }

    public int getScreenWidth() {
        return screen.getScreenWidth();
    }

    public int getScreenHeight() {
        return screen.getScreenHeight();
    }

    public void setShouldAutoUpdateCameraAspect(boolean shouldAutoUpdateCameraAspect) {
        this.shouldAutoUpdateCameraAspect = shouldAutoUpdateCameraAspect;
    }

    public boolean shouldAutoUpdateCameraAspect() {
        return shouldAutoUpdateCameraAspect;
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

    public void syncCamera(Camera camera) {
        this.camera.setEye(new Vector3f(camera.getEye()));
        this.camera.setCenter(new Vector3f(camera.getCenter()));
        this.camera.setUp(new Vector3f(camera.getUp()));
    }

    public Fog getFog() {
        return fog;
    }

    public void setFog(Fog fog) {
        this.fog = fog;
    }

    public Vector3f getParallelLightAmbientColor() {
        return parallelLightAmbientColor;
    }

    public Vector3f getPointLightAmbientColor() {
        return pointLightAmbientColor;
    }

    public void setPointLightAmbientColor(Vector3f pointLightAmbientColor) {
        this.pointLightAmbientColor = pointLightAmbientColor;
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

    public SimpleBlurInfo getSimpleBlurInfo() {
        return simpleBlurInfo;
    }

    public void setSimpleBlurInfo(SimpleBlurInfo simpleBlurInfo) {
        this.simpleBlurInfo = simpleBlurInfo;
    }

    public int getNumParallelLights() {
        return parallelLights.size();
    }

    public ParallelLight getParallelLight(int index) {
        return parallelLights.get(index);
    }

    public ParallelLight createParallelLight() {
        if (parallelLights.size() >= ParallelLightNabor.MAX_NUM_LIGHTS) {
            String msg = String.format("Cannot create more than %d lights", ParallelLightNabor.MAX_NUM_LIGHTS);
            throw new RuntimeException(msg);
        }

        var parallelLight = new ParallelLight();
        parallelLights.add(parallelLight);

        return parallelLight;
    }

    public boolean removeParallelLight(ParallelLight parallelLight) {
        return parallelLights.remove(parallelLight);
    }

    public int getNumPointLights() {
        return pointLights.size();
    }

    public PointLight getPointLight(int index) {
        return pointLights.get(index);
    }

    public PointLight createPointLight() {
        if (pointLights.size() >= PointLightNabor.MAX_NUM_LIGHTS) {
            String msg = String.format("Cannot create more than %d lights", PointLightNabor.MAX_NUM_LIGHTS);
            throw new RuntimeException(msg);
        }

        var pointLight = new PointLight();
        pointLights.add(pointLight);

        return pointLight;
    }

    public boolean removePointLight(PointLight pointLight) {
        return pointLights.remove(pointLight);
    }

    public int getNumSpotlights() {
        return spotlights.size();
    }

    public Spotlight getSpotlight(int index) {
        return spotlights.get(index);
    }

    public Spotlight createSpotlight() {
        if (spotlights.size() >= SpotlightNabor.MAX_NUM_LIGHTS) {
            String msg = String.format("Cannot create more than %d lights", SpotlightNabor.MAX_NUM_LIGHTS);
            throw new RuntimeException(msg);
        }

        var spotlight = new Spotlight();
        spotlights.add(spotlight);

        return spotlight;
    }

    public boolean removeSpotlight(Spotlight spotlight) {
        return spotlights.remove(spotlight);
    }
}
