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
 * @author maeda
 */
public class MttSettings {
    public class WindowSettings {
        public String title = "Mechtatel";
        public int width = 1280;
        public int height = 720;
        public boolean resizable = true;

        @Override
        public String toString() {
            String ret = String.format(
                    "Window Settings: {title=%s, width=%d, height=%d, resizable=%b}",
                    title, width, height, resizable);
            return ret;
        }
    }

    public class SystemSettings {
        public int fps = 60;

        @Override
        public String toString() {
            String ret = String.format("System settings: {fps=%d}", fps);
            return ret;
        }
    }

    public WindowSettings windowSettings;
    public SystemSettings systemSettings;

    public MttSettings() {
        windowSettings = new WindowSettings();
        systemSettings = new SystemSettings();
    }

    public MttSettings(String jsonFilepath) throws IOException {
        windowSettings = new WindowSettings();
        systemSettings = new SystemSettings();

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
    }

    @Override
    public String toString() {
        String ret = windowSettings.toString() + " " + systemSettings.toString();
        return ret;
    }
}
