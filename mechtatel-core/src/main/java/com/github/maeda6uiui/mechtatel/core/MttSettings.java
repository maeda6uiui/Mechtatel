package com.github.maeda6uiui.mechtatel.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
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

        public WindowSettings() {
            title = "Mechtatel";
            width = 1280;
            height = 720;
            resizable = true;
        }

        @Override
        public String toString() {
            return "WindowSettings{" +
                    "title='" + title + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", resizable=" + resizable +
                    '}';
        }
    }

    public static class SystemSettings {
        public int fps;

        public SystemSettings() {
            fps = 60;
        }

        @Override
        public String toString() {
            return "SystemSettings{" +
                    "fps=" + fps +
                    '}';
        }
    }

    public static class RenderingSettings {
        public String imageFormat;

        public RenderingSettings() {
            imageFormat = "srgb";
        }

        @Override
        public String toString() {
            return "RenderingSettings{" +
                    "imageFormat='" + imageFormat + '\'' +
                    '}';
        }
    }

    public static class VulkanSettings {
        public boolean enableValidationLayer;
        public boolean useGraphicsQueueAsPresentQueue;
        public int albedoMSAASamples;

        public VulkanSettings() {
            enableValidationLayer = false;
            useGraphicsQueueAsPresentQueue = false;
            albedoMSAASamples = 2;
        }

        @Override
        public String toString() {
            return "VulkanSettings{" +
                    "enableValidationLayer=" + enableValidationLayer +
                    ", useGraphicsQueueAsPresentQueue=" + useGraphicsQueueAsPresentQueue +
                    ", albedoMSAASamples=" + albedoMSAASamples +
                    '}';
        }
    }

    @JsonProperty("window")
    public WindowSettings windowSettings;
    @JsonProperty("system")
    public SystemSettings systemSettings;
    @JsonProperty("rendering")
    public RenderingSettings renderingSettings;
    @JsonProperty("vulkan")
    public VulkanSettings vulkanSettings;

    private static MttSettings instance;

    public MttSettings() {
        windowSettings = new WindowSettings();
        systemSettings = new SystemSettings();
        renderingSettings = new RenderingSettings();
        vulkanSettings = new VulkanSettings();
    }

    /**
     * Loads settings from a JSON file.
     * This method returns empty value if setting file specified does not exist
     * or if it fails to load settings from the file.
     *
     * @param jsonFilepath Filepath of the JSON file
     * @return Settings
     */
    public static Optional<MttSettings> load(String jsonFilepath) {
        Path settingsFile = Paths.get(jsonFilepath);
        if (!Files.exists(settingsFile)) {
            logger.error("Setting file ({}) was not found", settingsFile);
            return Optional.empty();
        }

        try {
            String json = Files.readString(settingsFile);

            var mapper = new ObjectMapper();
            instance = mapper.readValue(json, MttSettings.class);
            return Optional.of(instance);
        } catch (IOException e) {
            logger.error("Failed to load setting file", e);
            return Optional.empty();
        }
    }

    /**
     * Returns currently retained {@link MttSettings} instance.
     *
     * @return Settings
     */
    public static Optional<MttSettings> get() {
        return Optional.ofNullable(instance);
    }

    @Override
    public String toString() {
        return "MttSettings{" +
                "windowSettings=" + windowSettings +
                ", systemSettings=" + systemSettings +
                ", renderingSettings=" + renderingSettings +
                ", vulkanSettings=" + vulkanSettings +
                '}';
    }
}
