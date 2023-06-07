package com.github.maeda6uiui.mechtatel.core.animation;

import org.joml.Vector3f;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Info for model animation
 *
 * @author maeda6uiui
 */
public class AnimationInfo {
    public static class InitialProperties {
        public Vector3f position;
        public Vector3f rotation;
        public String rotationApplyOrder;
        public Vector3f scale;
        public String referenceTo;
        public List<String> applyOrder;

        public InitialProperties() {
            position = new Vector3f(0.0f, 0.0f, 0.0f);
            rotation = new Vector3f(0.0f, 0.0f, 0.0f);
            rotationApplyOrder = "xyz";
            scale = new Vector3f(1.0f, 1.0f, 1.0f);
            referenceTo = "self";
            applyOrder = new ArrayList<>();
        }
    }

    public static class Model {
        public String name;
        public String filename;
        public InitialProperties initialProperties;

        public Model() {
            name = "";
            filename = "";
            initialProperties = new InitialProperties();
        }
    }

    public static class Displacement {
        public Vector3f translation;
        public Vector3f rotation;
        public String rotationApplyOrder;
        public Vector3f rescale;
        public String referenceTo;
        public List<String> applyOrder;

        public Displacement() {
            translation = new Vector3f(0.0f, 0.0f, 0.0f);
            rotation = new Vector3f(0.0f, 0.0f, 0.0f);
            rotationApplyOrder = "xyz";
            rescale = new Vector3f(1.0f, 1.0f, 1.0f);
            referenceTo = "self";
            applyOrder = new ArrayList<>();
        }
    }

    public static class RevertDisplacement {
        public int frameIndex;

        public RevertDisplacement() {
            frameIndex = -1;
        }
    }

    public static class KeyFrame {
        public int frameIndex;
        public float time;
        public int nextFrameIndex;
        public Displacement displacement;
        public RevertDisplacement revertDisplacement;

        public KeyFrame() {
            frameIndex = 0;
            time = 0.0f;
            nextFrameIndex = 1;
            displacement = new Displacement();
            revertDisplacement = new RevertDisplacement();
        }
    }

    public static class Animation {
        public String name;
        public List<String> models;
        public Map<Integer, KeyFrame> keyFrames;

        public Animation() {
            name = "";
            models = new ArrayList<>();
            keyFrames = new HashMap<>();
        }
    }

    private String name;
    private String animationDirname;
    private Map<String, Model> models;

    private Map<String, Animation> animations;

    public AnimationInfo(String jsonFilepath) throws IOException {
        animationDirname = Paths.get(jsonFilepath).getParent().toString();

        RawAnimationInfo rawAnimInfo = RawAnimationInfo.load(jsonFilepath);
        name = rawAnimInfo.name;

        models = new HashMap<>();
        for (var rawModel : rawAnimInfo.models) {
            var model = new Model();

            model.name = rawModel.name;
            model.filename = rawModel.filename;
            model.initialProperties.position = new Vector3f(
                    rawModel.initialProperties.position.x,
                    rawModel.initialProperties.position.y,
                    rawModel.initialProperties.position.z
            );
            model.initialProperties.rotation = new Vector3f(
                    rawModel.initialProperties.rotation.x,
                    rawModel.initialProperties.rotation.y,
                    rawModel.initialProperties.rotation.z
            );
            model.initialProperties.rotationApplyOrder = rawModel.initialProperties.rotation.applyOrder;
            model.initialProperties.scale = new Vector3f(
                    rawModel.initialProperties.scale.x,
                    rawModel.initialProperties.scale.y,
                    rawModel.initialProperties.scale.z
            );
            model.initialProperties.referenceTo = rawModel.initialProperties.referenceTo;
            model.initialProperties.applyOrder = rawModel.initialProperties.applyOrder;

            models.put(rawModel.name, model);
        }

        animations = new HashMap<>();
        for (var rawAnimation : rawAnimInfo.animations) {
            var animation = new Animation();

            animation.name = rawAnimation.name;
            animation.models = rawAnimation.models;

            for (var rawKeyFrame : rawAnimation.keyFrames) {
                var keyFrame = new KeyFrame();

                keyFrame.frameIndex = rawKeyFrame.frameIndex;
                keyFrame.time = rawKeyFrame.time;
                keyFrame.nextFrameIndex = rawKeyFrame.nextFrameIndex;

                if (rawKeyFrame.displacement != null) {
                    keyFrame.displacement.translation = new Vector3f(
                            rawKeyFrame.displacement.translation.x,
                            rawKeyFrame.displacement.translation.y,
                            rawKeyFrame.displacement.translation.z
                    );
                    keyFrame.displacement.rotation = new Vector3f(
                            rawKeyFrame.displacement.rotation.x,
                            rawKeyFrame.displacement.rotation.y,
                            rawKeyFrame.displacement.rotation.z
                    );
                    keyFrame.displacement.rotationApplyOrder = rawKeyFrame.displacement.rotation.applyOrder;
                    keyFrame.displacement.rescale = new Vector3f(
                            rawKeyFrame.displacement.rescale.x,
                            rawKeyFrame.displacement.rescale.y,
                            rawKeyFrame.displacement.rescale.z
                    );
                    keyFrame.displacement.referenceTo = rawKeyFrame.displacement.referenceTo;
                    keyFrame.displacement.applyOrder = rawKeyFrame.displacement.applyOrder;

                    keyFrame.revertDisplacement = null;
                } else if (rawKeyFrame.revertDisplacement != null) {
                    keyFrame.revertDisplacement.frameIndex = rawKeyFrame.revertDisplacement.frameIndex;
                    keyFrame.displacement = null;
                } else {
                    throw new RuntimeException("Either displacement or revertDisplacement must be specified");
                }

                animation.keyFrames.put(rawKeyFrame.frameIndex, keyFrame);
            }

            animations.put(rawAnimation.name, animation);
        }
    }

    public String getName() {
        return name;
    }

    public String getAnimationDirname() {
        return animationDirname;
    }

    public Map<String, Model> getModels() {
        return new HashMap<>(models);
    }

    public Map<String, Animation> getAnimations() {
        return new HashMap<>(animations);
    }
}
