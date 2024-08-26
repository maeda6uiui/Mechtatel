package com.github.maeda6uiui.mechtatel.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

/**
 * Settings for Mechtatel
 *
 * @author maeda6uiui
 */
public class MttSettings {
    private static final Logger logger = LoggerFactory.getLogger(MttSettings.class);

    public static class WindowSettings {
        public String title;
        public int width;
        public int height;
        public boolean resizable;
        public boolean fullScreen;
        public boolean windowedFullScreen;
        public int monitorIndex;

        public WindowSettings() {
            title = "Mechtatel";
            width = 1280;
            height = 720;
            resizable = true;
            fullScreen = false;
            windowedFullScreen = false;
            monitorIndex = 0;
        }
    }

    public static class HeadlessSettings {
        public int width;
        public int height;
        public boolean useOpenAL;

        public HeadlessSettings() {
            width = 1280;
            height = 720;
            useOpenAL = false;
        }
    }

    public static class SystemSettings {
        public int fps;

        public SystemSettings() {
            fps = 60;
        }
    }

    public static class RenderingSettings {
        public RenderingEngine engine;
        public MttTexture.ImageFormat imageFormat;

        public RenderingSettings() {
            engine = RenderingEngine.VULKAN;
            imageFormat = MttTexture.ImageFormat.SRGB;
        }
    }

    public static class TextureOperationSettings {
        public int width;
        public int height;
        public boolean changeExtentOnRecreate;

        public TextureOperationSettings() {
            width = -1;
            height = -1;
            changeExtentOnRecreate = true;
        }
    }

    public static class VulkanSettings {
        public boolean enableValidationLayer;
        public int preferablePhysicalDeviceIndex;
        public int preferableGraphicsFamilyIndex;
        public int preferablePresentFamilyIndex;
        public int albedoMSAASamples;

        public VulkanSettings() {
            enableValidationLayer = false;
            preferablePhysicalDeviceIndex = 0;
            preferableGraphicsFamilyIndex = -1;
            preferablePresentFamilyIndex = -1;
            albedoMSAASamples = 2;
        }
    }

    @JsonProperty("window")
    public WindowSettings windowSettings;
    @JsonProperty("headless")
    public HeadlessSettings headlessSettings;
    @JsonProperty("system")
    public SystemSettings systemSettings;
    @JsonProperty("rendering")
    public RenderingSettings renderingSettings;
    @JsonProperty("textureOperation")
    public TextureOperationSettings textureOperation;
    @JsonProperty("vulkan")
    public VulkanSettings vulkanSettings;

    private static MttSettings instance;

    public MttSettings() {
        windowSettings = new WindowSettings();
        headlessSettings = new HeadlessSettings();
        systemSettings = new SystemSettings();
        renderingSettings = new RenderingSettings();
        textureOperation = new TextureOperationSettings();
        vulkanSettings = new VulkanSettings();
    }

    /**
     * Loads settings from a JSON file.
     * This method returns empty value if the file specified as {@code jsonFile} does not exist
     * or if it fails to load settings from the file specified.
     *
     * @param jsonFile Path of the setting file
     * @return Settings
     */
    public static Optional<MttSettings> load(Path jsonFile) {
        if (!Files.exists(jsonFile)) {
            logger.error("Setting file ({}) was not found", jsonFile);
            return Optional.empty();
        }

        try {
            String json = Files.readString(jsonFile);
            instance = new ObjectMapper().readValue(json, MttSettings.class);
            return Optional.of(instance);
        } catch (IOException e) {
            logger.error("Failed to load setting file", e);
            return Optional.empty();
        }
    }

    /**
     * Loads settings from a JSON file.
     *
     * @param jsonFilepath Filepath of the setting file
     * @return Settings
     * @see #load(Path)
     */
    public static Optional<MttSettings> load(String jsonFilepath) {
        return load(Paths.get(jsonFilepath));
    }

    /**
     * Returns currently retained settings instance.
     *
     * @return Settings
     */
    public static Optional<MttSettings> get() {
        return Optional.ofNullable(instance);
    }
}
