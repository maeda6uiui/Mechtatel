package com.github.maeda6uiui.mechtatel.core.animation;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

/**
 * Raw info for model animation
 *
 * @author maeda6uiui
 */
class RawAnimationInfo {
    public static class Position {
        public float x;
        public float y;
        public float z;
    }

    public static class Rotation {
        public float x;
        public float y;
        public float z;
        public String applyOrder;
    }

    public static class Scale {
        public float x;
        public float y;
        public float z;
    }

    public static class InitialProperties {
        public Position position;
        public Rotation rotation;
        public Scale scale;
        public String referenceTo;
    }

    public static class Model {
        public String name;
        public String filename;
        public InitialProperties initialProperties;
    }

    public static class Translation {
        public float x;
        public float y;
        public float z;
    }

    public static class Displacement {
        public Translation translation;
        public Rotation rotation;
        public String referenceTo;
    }

    public static class KeyFrame {
        public int frameIndex;
        public float duration;
        public int nextFrameIndex;
        public Displacement displacement;
    }

    public static class Animation {
        public String name;
        public boolean resetModelsOnRestart;
        public List<String> models;
        public List<KeyFrame> keyFrames;
    }

    public String name;
    public List<Model> models;
    public List<Animation> animations;

    public static RawAnimationInfo load(String jsonFilepath) throws IOException {
        List<String> lines = Files.readAllLines(Paths.get(jsonFilepath));

        var sb = new StringBuilder();
        lines.forEach(line -> {
            sb.append(line);
            sb.append("\n");
        });
        String json = sb.toString();

        var mapper = new ObjectMapper();
        RawAnimationInfo animInfo = mapper.readValue(json, RawAnimationInfo.class);

        return animInfo;
    }
}
