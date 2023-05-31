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
    }

    public class SystemSettings {
        public int fps = 60;
    }

    public class RenderingSettings {
        public String imageFormat = "srgb";
    }

    public class BulletSettings {
        public boolean dist;
        public String dirname;
        public String buildType;
        public String flavor;
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
}
