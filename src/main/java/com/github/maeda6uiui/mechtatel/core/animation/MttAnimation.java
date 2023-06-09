package com.github.maeda6uiui.mechtatel.core.animation;

import com.github.maeda6uiui.mechtatel.core.component.MttModel3D;
import com.github.maeda6uiui.mechtatel.core.component.MttModel3DSet;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

/**
 * Model animation
 *
 * @author maeda6uiui
 */
public class MttAnimation {
    public static class AnimationPlayInfo {
        public boolean playing;
        public float lastTime;

        public int currentFrameIndex;
        public float currentFrameStartTime;
        public float currentFrameDuration;
        public int nextFrameIndex;
    }

    private AnimationInfo animationInfo;
    private Map<String, MttModel3D> models; //(model name, model)
    private Map<String, MttModel3DSet> modelSets;   //(animation name, models per animation)
    private Map<String, AnimationPlayInfo> animationPlayInfos;

    private char[] getRotationApplyOrderCs(String rotationApplyOrder) {
        if (rotationApplyOrder.length() != 3) {
            throw new RuntimeException(
                    "Rotation apply order must be 3-letter representation such as xyz");
        }

        var cs = new char[3];
        for (int i = 0; i < 3; i++) {
            cs[i] = rotationApplyOrder.charAt(i);
            if (!(cs[i] == 'x' || cs[i] == 'y' || cs[i] == 'z')) {
                throw new RuntimeException(
                        "Element of rotation apply order must be one of x,y, and z");
            }
        }

        return cs;
    }

    private void applyRotationToModel(MttModel3D model, Vector3fc rotation, String rotationApplyOrder) {
        char[] cs = this.getRotationApplyOrderCs(rotationApplyOrder);
        for (char c : cs) {
            if (c == 'x') {
                model.rotX(rotation.x());
            } else if (c == 'y') {
                model.rotY(rotation.y());
            } else if (c == 'z') {
                model.rotZ(rotation.z());
            }
        }
    }

    private void applyInitialProperties(MttModel3D model, AnimationInfo.InitialProperties initialProperties) {
        if (!(initialProperties.referenceTo.equals("origin") || initialProperties.referenceTo.equals("self"))) {
            throw new RuntimeException("Unsupported reference type specified: " + initialProperties.referenceTo);
        }

        model.setMat(new Matrix4f().identity());

        if (initialProperties.referenceTo.equals("origin")) {
            model.translate(initialProperties.position);
            model.rescale(initialProperties.scale);
            this.applyRotationToModel(model, initialProperties.rotation, initialProperties.rotationApplyOrder);
        } else if (initialProperties.referenceTo.equals("self")) {
            model.rescale(initialProperties.scale);
            this.applyRotationToModel(model, initialProperties.rotation, initialProperties.rotationApplyOrder);
            model.translate(initialProperties.position);
        }
    }

    public MttAnimation(
            MttVulkanInstance vulkanInstance, String screenName, AnimationInfo animationInfo) throws IOException {
        this.animationInfo = animationInfo;

        //Load models
        String animationDirname = animationInfo.getAnimationDirname();

        models = new HashMap<>();
        for (var entry : animationInfo.getModels().entrySet()) {
            String modelName = entry.getKey();
            AnimationInfo.Model animModel = entry.getValue();

            String modelFilepath = Paths.get(animationDirname, animModel.filename).toString();
            MttModel3D model = new MttModel3D(vulkanInstance, screenName, modelFilepath);
            this.applyInitialProperties(model, animModel.initialProperties);

            models.put(modelName, model);
        }

        //Create a model set per animation
        modelSets = new HashMap<>();
        for (var entry : animationInfo.getAnimations().entrySet()) {
            String animationName = entry.getKey();
            AnimationInfo.Animation animation = entry.getValue();

            var modelSet = new MttModel3DSet();
            for (String modelName : animation.models) {
                MttModel3D model = models.get(modelName);
                modelSet.add(model);
            }

            modelSets.put(animationName, modelSet);
        }

        animationPlayInfos = new HashMap<>();
    }

    public MttAnimation(
            MttVulkanInstance vulkanInstance, AnimationInfo animationInfo, Map<String, MttModel3D> srcModels) {
        this.animationInfo = animationInfo;

        //Duplicate models
        models = new HashMap<>();
        srcModels.forEach((modelName, srcModel) -> {
            MttModel3D model = new MttModel3D(vulkanInstance, srcModel);
            models.put(modelName, model);
        });

        //Apply initial properties
        for (var entry : animationInfo.getModels().entrySet()) {
            String modelName = entry.getKey();
            AnimationInfo.Model animModel = entry.getValue();

            MttModel3D model = models.get(modelName);
            this.applyInitialProperties(model, animModel.initialProperties);
        }

        //Create a model set per animation
        modelSets = new HashMap<>();
        for (var entry : animationInfo.getAnimations().entrySet()) {
            String animationName = entry.getKey();
            AnimationInfo.Animation animation = entry.getValue();

            var modelSet = new MttModel3DSet();
            for (String modelName : animation.models) {
                MttModel3D model = models.get(modelName);
                modelSet.add(model);
            }

            modelSets.put(animationName, modelSet);
        }

        animationPlayInfos = new HashMap<>();
    }

    public void cleanup() {
        models.forEach((k, v) -> v.cleanup());
    }

    public void startAnimation(String animationName) {
        var animationPlayInfo = new AnimationPlayInfo();

        float curTime = (float) glfwGetTime();

        animationPlayInfo.playing = true;
        animationPlayInfo.lastTime = curTime;

        AnimationInfo.Animation animation = animationInfo.getAnimations().get(animationName);
        AnimationInfo.KeyFrame startKeyFrame = animation.keyFrames.get(0);

        animationPlayInfo.currentFrameIndex = 0;
        animationPlayInfo.currentFrameStartTime = curTime;
        animationPlayInfo.currentFrameDuration = startKeyFrame.duration;
        animationPlayInfo.nextFrameIndex = startKeyFrame.nextFrameIndex;

        animationPlayInfos.put(animationName, animationPlayInfo);
    }

    public boolean restartAnimation(String animationName) {
        if (!animationPlayInfos.containsKey(animationName)) {
            return false;
        }

        boolean resetModelsToInitialState = animationInfo.getAnimations().get(animationName).resetModelsOnRestart;
        if (resetModelsToInitialState) {
            var modelSet = new MttModel3DSet();

            List<String> modelNames = animationInfo.getAnimations().get(animationName).models;
            modelNames.forEach(modelName -> {
                AnimationInfo.InitialProperties initialProperties = animationInfo.getModels().get(modelName).initialProperties;
                MttModel3D model = models.get(modelName);
                this.applyInitialProperties(model, initialProperties);

                modelSet.add(model);
            });

            modelSets.put(animationName, modelSet);
        }

        this.startAnimation(animationName);

        return true;
    }

    public boolean stopAnimation(String animationName) {
        if (!animationPlayInfos.containsKey(animationName)) {
            return false;
        }

        List<String> modelNames = animationInfo.getAnimations().get(animationName).models;
        modelNames.forEach(modelName -> {
            AnimationInfo.InitialProperties initialProperties = animationInfo.getModels().get(modelName).initialProperties;
            MttModel3D model = models.get(modelName);
            this.applyInitialProperties(model, initialProperties);
        });

        animationPlayInfos.remove(animationName);

        return true;
    }

    public void stopAllAnimations() {
        models.forEach((modelName, model) -> {
            AnimationInfo.InitialProperties initialProperties = animationInfo.getModels().get(modelName).initialProperties;
            this.applyInitialProperties(model, initialProperties);
        });

        animationPlayInfos.clear();
    }

    public boolean pauseAnimation(String animationName) {
        if (!animationPlayInfos.containsKey(animationName)) {
            return false;
        }

        AnimationPlayInfo animationPlayInfo = animationPlayInfos.get(animationName);
        animationPlayInfo.playing = false;

        return true;
    }

    public boolean resumeAnimation(String animationName) {
        if (!animationPlayInfos.containsKey(animationName)) {
            return false;
        }

        AnimationPlayInfo animationPlayInfo = animationPlayInfos.get(animationName);
        animationPlayInfo.playing = true;

        return true;
    }

    public boolean isAnimationPlaying(String animationName) {
        if (!animationPlayInfos.containsKey(animationName)) {
            return false;
        }

        AnimationPlayInfo animationPlayInfo = animationPlayInfos.get(animationName);
        return animationPlayInfo.playing;
    }

    private void applyRotationToModelSet(MttModel3DSet modelSet, Vector3fc rotation, String rotationApplyOrder) {
        char[] cs = this.getRotationApplyOrderCs(rotationApplyOrder);
        for (char c : cs) {
            if (c == 'x') {
                modelSet.rotX(rotation.x());
            } else if (c == 'y') {
                modelSet.rotY(rotation.y());
            } else if (c == 'z') {
                modelSet.rotZ(rotation.z());
            }
        }
    }

    private void applyDisplacement(
            MttModel3DSet modelSet,
            AnimationInfo.Displacement displacement,
            float frameDuration,
            float timeElapsed) {
        if (!(displacement.referenceTo.equals("origin") || displacement.referenceTo.equals("self"))) {
            throw new RuntimeException("Unsupported reference type specified: " + displacement.referenceTo);
        }

        //Get displacement per time elapsed
        var translationPerSecond = new Vector3f(displacement.translation).div(frameDuration);
        var rotationPerSecond = new Vector3f(displacement.rotation).div(frameDuration);

        var translationPerTimeElapsed = new Vector3f(translationPerSecond).mul(timeElapsed);
        var rotationPerTimeElapsed = new Vector3f(rotationPerSecond).mul(timeElapsed);

        //Apply displacement to models
        if (displacement.referenceTo.equals("origin")) {
            modelSet.translate(translationPerTimeElapsed);
            this.applyRotationToModelSet(modelSet, rotationPerTimeElapsed, displacement.rotationApplyOrder);
        } else if (displacement.referenceTo.equals("self")) {
            //First move the models to the origin
            Vector3f originalPosition = modelSet.getPosition();
            modelSet.translate(new Vector3f(originalPosition).mul(-1.0f));

            //Then apply rotation
            this.applyRotationToModelSet(modelSet, rotationPerTimeElapsed, displacement.rotationApplyOrder);

            //Move the models back to the original position
            modelSet.translate(originalPosition);

            //Apply translation
            modelSet.translate(translationPerTimeElapsed);
        }
    }

    public void update() {
        animationPlayInfos.forEach((animationName, animationPlayInfo) -> {
            AnimationInfo.Animation animation = animationInfo.getAnimations().get(animationName);

            float curTime = (float) glfwGetTime();
            if (animationPlayInfo.playing) {
                float timeElapsed = curTime - animationPlayInfo.lastTime;
                AnimationInfo.KeyFrame currentKeyFrame = animation.keyFrames.get(animationPlayInfo.currentFrameIndex);

                MttModel3DSet modelSet = modelSets.get(animationName);
                this.applyDisplacement(
                        modelSet,
                        currentKeyFrame.displacement,
                        animationPlayInfo.currentFrameDuration,
                        timeElapsed);
            }

            animationPlayInfo.lastTime = curTime;

            //Switch to next frame
            if (curTime > animationPlayInfo.currentFrameStartTime + animationPlayInfo.currentFrameDuration) {
                if (animationPlayInfo.nextFrameIndex < 0) {
                    this.stopAnimation(animationName);
                } else {
                    AnimationInfo.KeyFrame currentKeyFrame = animation.keyFrames.get(animationPlayInfo.nextFrameIndex);
                    AnimationInfo.KeyFrame nextKeyFrame = animation.keyFrames.get(currentKeyFrame.nextFrameIndex);

                    animationPlayInfo.currentFrameIndex = animationPlayInfo.nextFrameIndex;
                    animationPlayInfo.currentFrameStartTime = curTime;
                    animationPlayInfo.currentFrameDuration = nextKeyFrame.duration;
                    animationPlayInfo.nextFrameIndex = currentKeyFrame.nextFrameIndex;
                }

                if (animationPlayInfo.currentFrameIndex == 0) {
                    this.restartAnimation(animationName);
                }
            }
        });
    }

    public Map<String, MttModel3D> getModels() {
        return new HashMap<>(models);
    }

    public Map<String, MttModel3DSet> getModelSets() {
        return new HashMap<>(modelSets);
    }
}
