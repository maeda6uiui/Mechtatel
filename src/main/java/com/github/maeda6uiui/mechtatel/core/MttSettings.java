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

    public static class BulletSettings {
        public boolean dist;
        public String dirname;
        public String buildType;
        public String flavor;

        public BulletSettings() {
            dist = true;
            dirname = "./Mechtatel/Bin";
            buildType = "Debug";
            flavor = "Sp";
        }

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
