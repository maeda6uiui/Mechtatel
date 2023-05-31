package com.github.maeda6uiui.mechtatel.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Settings for Mechtatel
 *
 * @author maeda6uiui
 */
public class MttSettings {
    public class WindowSettings {
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

    public class SystemSettings {
        public int fps = 60;

        @Override
        public String toString() {
            return "SystemSettings{" +
                    "fps=" + fps +
                    '}';
        }
    }

    public class RenderingSettings {
        public String imageFormat = "srgb";

        @Override
        public String toString() {
            return "RenderingSettings{" +
                    "imageFormat='" + imageFormat + '\'' +
                    '}';
        }
    }

    public class BulletSettings {
        public boolean dist;
        public String dirname;
        public String buildType;
        public String flavor;

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

    public WindowSettings windowSettings;
    public SystemSettings systemSettings;
    public RenderingSettings renderingSettings;
    public BulletSettings bulletSettings;

    public MttSettings() {
        windowSettings = new WindowSettings();
        systemSettings = new SystemSettings();
        renderingSettings = new RenderingSettings();
        bulletSettings = new BulletSettings();
    }

    public MttSettings(String jsonFilepath) throws IOException {
        windowSettings = new WindowSettings();
        systemSettings = new SystemSettings();
        renderingSettings = new RenderingSettings();
        bulletSettings = new BulletSettings();

        List<String> lines = Files.readAllLines(Paths.get(jsonFilepath));

        var sb = new StringBuilder();
        lines.forEach(line -> {
            sb.append(line);
            sb.append("\n");
        });
        String json = sb.toString();

        var mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(json);

        JsonNode windowNode = node.get("window");
        windowSettings.title = windowNode.get("title").asText();
        windowSettings.width = windowNode.get("width").asInt();
        windowSettings.height = windowNode.get("height").asInt();
        windowSettings.resizable = windowNode.get("resizable").asBoolean();

        JsonNode systemNode = node.get("system");
        systemSettings.fps = systemNode.get("fps").asInt();

        JsonNode renderingNode = node.get("rendering");
        renderingSettings.imageFormat = renderingNode.get("imageFormat").asText();

        JsonNode bulletNode = node.get("bullet");
        bulletSettings.dist = bulletNode.get("dist").asBoolean();
        bulletSettings.dirname = bulletNode.get("dirname").asText();
        bulletSettings.buildType = bulletNode.get("buildType").asText();
        bulletSettings.flavor = bulletNode.get("flavor").asText();
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
