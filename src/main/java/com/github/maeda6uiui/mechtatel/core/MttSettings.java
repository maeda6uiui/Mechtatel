package com.github.maeda6uiui.mechtatel.core;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Settings for Mechtatel
 *
 * @author maeda6uiui
 */
public class MttSettings {
    public static class WindowSettings {
        public String title = "Mechtatel";
        public int width = 1280;
        public int height = 720;
        public boolean resizable = true;

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
        public int fps = 60;
        public boolean runGatewayServer = false;

        @Override
        public String toString() {
            return "SystemSettings{" +
                    "fps=" + fps +
                    '}';
        }
    }

    public static class RenderingSettings {
        public String imageFormat = "srgb";

        @Override
        public String toString() {
            return "RenderingSettings{" +
                    "imageFormat='" + imageFormat + '\'' +
                    '}';
        }
    }

    public static class BulletSettings {
        public boolean dist = true;
        public String dirname = "./Mechtatel/Bin";
        public String buildType = "Debug";
        public String flavor = "Sp";

        @Override
        public String toString() {
            return "BulletSettings{" +
                    "dist=" + dist +
                    ", dirname='" + dirname + '\'' +
                    ", buildType='" + buildType + '\'' +
                    ", flavor='" + flavor + '\'' +
                    '}';
        }
    }

    @JsonProperty("window")
    public WindowSettings windowSettings;
    @JsonProperty("system")
    public SystemSettings systemSettings;
    @JsonProperty("rendering")
    public RenderingSettings renderingSettings;
    @JsonProperty("bullet")
    public BulletSettings bulletSettings;

    public MttSettings() {
        windowSettings = new WindowSettings();
        systemSettings = new SystemSettings();
        renderingSettings = new RenderingSettings();
        bulletSettings = new BulletSettings();
    }

    public static MttSettings load(String jsonFilepath) throws IOException {
        String json = Files.readString(Paths.get(jsonFilepath));

        var mapper = new ObjectMapper();
        MttSettings settings = mapper.readValue(json, MttSettings.class);

        return settings;
    }

    @Override
    public String toString() {
        return "MttSettings{" +
                "windowSettings=" + windowSettings +
                ", systemSettings=" + systemSettings +
                ", renderingSettings=" + renderingSettings +
                ", bulletSettings=" + bulletSettings +
                '}';
    }
}
