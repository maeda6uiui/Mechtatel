package com.github.maeda6uiui.mechtatel.core.animation;

import com.github.maeda6uiui.mechtatel.core.component.MttComponentSet;
import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import org.joml.Vector3f;
import org.joml.Vector3fc;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
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
    private Map<String, MttModel> models; //(model name, model)
    private Map<String, MttComponentSet> modelSets;   //(animation name, models per animation)
    private Map<String, AnimationPlayInfo> animationPlayInfos;

    public MttAnimation(
            MttVulkanInstance vulkanInstance, String screenName, AnimationInfo animationInfo) throws IOException {
        this.animationInfo = animationInfo;

        //Load models
        URL animationDirectory = animationInfo.getAnimationDirectory();

        models = new HashMap<>();
        for (var entry : animationInfo.getModels().entrySet()) {
            String modelName = entry.getKey();
            AnimationInfo.Model animModel = entry.getValue();

            Path modelFile = Paths.get(animationDirectory.getFile(), animModel.filename);
            MttModel model = new MttModel(vulkanInstance, screenName, modelFile.toUri().toURL());
            models.put(modelName, model);
        }

        //Create a model set per animation
        modelSets = new HashMap<>();
        for (var entry : animationInfo.getAnimations().entrySet()) {
            String animationName = entry.getKey();
            AnimationInfo.Animation animation = entry.getValue();

            var modelSet = new MttComponentSet<MttModel>();
            for (String modelName : animation.models) {
                MttModel model = models.get(modelName);
                modelSet.add(model);
            }

            modelSets.put(animationName, modelSet);
        }

        animationPlayInfos = new HashMap<>();
    }

    public MttAnimation(
            MttVulkanInstance vulkanInstance, AnimationInfo animationInfo, Map<String, MttModel> srcModels) {
        this.animationInfo = animationInfo;

        //Duplicate models
        models = new HashMap<>();
        srcModels.forEach((modelName, srcModel) -> {
            MttModel model = new MttModel(vulkanInstance, srcModel);
            models.put(modelName, model);
        });

        //Create a model set per animation
        modelSets = new HashMap<>();
        for (var entry : animationInfo.getAnimations().entrySet()) {
            String animationName = entry.getKey();
            AnimationInfo.Animation animation = entry.getValue();

            var modelSet = new MttComponentSet<MttModel>();
            for (String modelName : animation.models) {
                MttModel model = models.get(modelName);
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

    private void resetAnimationModels(String animationName) {
        List<String> modelNames = animationInfo.getAnimations().get(animationName).models;
        modelNames.forEach(modelName -> {
            MttModel model = models.get(modelName);
            model.reset();
        });
    }

    public boolean restartAnimation(String animationName) {
        if (!animationPlayInfos.containsKey(animationName)) {
            return false;
        }

        this.resetAnimationModels(animationName);

        return true;
    }

    public boolean stopAnimation(String animationName) {
        if (!animationPlayInfos.containsKey(animationName)) {
            return false;
        }

        this.resetAnimationModels(animationName);
        animationPlayInfos.remove(animationName);

        return true;
    }

    public void stopAllAnimations() {
        models.forEach((modelName, model) -> model.reset());
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

    private void applyRotationToModelSet(MttComponentSet modelSet, Vector3fc rotation, String rotationApplyOrder) {
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
            MttComponentSet modelSet,
            AnimationInfo.Displacement displacement,
            float frameDuration,
            float timeElapsed) {
        //Get displacement per time elapsed
        var translationPerSecond = new Vector3f(displacement.translation).div(frameDuration);
        var rotationPerSecond = new Vector3f(displacement.rotation).div(frameDuration);

        var translationPerTimeElapsed = new Vector3f(translationPerSecond).mul(timeElapsed);
        var rotationPerTimeElapsed = new Vector3f(rotationPerSecond).mul(timeElapsed);

        //Apply displacement to models
        modelSet.translate(translationPerTimeElapsed);
        this.applyRotationToModelSet(modelSet, rotationPerTimeElapsed, displacement.rotationApplyOrder);
    }

    public void update() {
        animationPlayInfos.forEach((animationName, animationPlayInfo) -> {
            AnimationInfo.Animation animation = animationInfo.getAnimations().get(animationName);

            float curTime = (float) glfwGetTime();
            if (animationPlayInfo.playing) {
                float timeElapsed = curTime - animationPlayInfo.lastTime;
                AnimationInfo.KeyFrame currentKeyFrame = animation.keyFrames.get(animationPlayInfo.currentFrameIndex);

                MttComponentSet modelSet = modelSets.get(animationName);
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

    public Map<String, MttModel> getModels() {
        return new HashMap<>(models);
    }

    public Map<String, MttComponentSet> getModelSets() {
        return new HashMap<>(modelSets);
    }
}
