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
        public float accumulateTime;

        public int currentFrameIndex;
        public float currentFrameStartTime;
        public int nextFrameIndex;
        public float nextFrameStartTime;
    }

    private AnimationInfo animInfo;
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

    private void applyRotation(MttModel3D model, Vector3fc rotation, String rotationApplyOrder) {
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
            this.applyRotation(model, initialProperties.rotation, initialProperties.rotationApplyOrder);
        } else if (initialProperties.referenceTo.equals("self")) {
            model.rescale(initialProperties.scale);
            this.applyRotation(model, initialProperties.rotation, initialProperties.rotationApplyOrder);
            model.translate(initialProperties.position);
        }
    }

    public MttAnimation(MttVulkanInstance vulkanInstance, String screenName, AnimationInfo animInfo) throws IOException {
        this.animInfo = animInfo;

        //Load models
        String animationDirname = animInfo.getAnimationDirname();

        models = new HashMap<>();
        for (var entry : animInfo.getModels().entrySet()) {
            String modelName = entry.getKey();
            AnimationInfo.Model animModel = entry.getValue();

            String modelFilepath = Paths.get(animationDirname, animModel.filename).toString();
            MttModel3D model = new MttModel3D(vulkanInstance, screenName, modelFilepath);
            this.applyInitialProperties(model, animModel.initialProperties);

            models.put(modelName, model);
        }

        //Create a model set per animation
        modelSets = new HashMap<>();
        for (var entry : animInfo.getAnimations().entrySet()) {
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

    public MttAnimation(MttVulkanInstance vulkanInstance, AnimationInfo animInfo, Map<String, MttModel3D> srcModels) {
        this.animInfo = animInfo;

        //Duplicate models
        models = new HashMap<>();
        srcModels.forEach((modelName, srcModel) -> {
            MttModel3D model = new MttModel3D(vulkanInstance, srcModel);
            models.put(modelName, model);
        });

        //Apply initial properties
        for (var entry : animInfo.getModels().entrySet()) {
            String modelName = entry.getKey();
            AnimationInfo.Model animModel = entry.getValue();

            MttModel3D model = models.get(modelName);
            this.applyInitialProperties(model, animModel.initialProperties);
        }

        //Create a model set per animation
        modelSets = new HashMap<>();
        for (var entry : animInfo.getAnimations().entrySet()) {
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

        animationPlayInfo.playing = true;
        animationPlayInfo.lastTime = (float) glfwGetTime();
        animationPlayInfo.accumulateTime = 0.0f;

        AnimationInfo.Animation animation = animInfo.getAnimations().get(animationName);
        AnimationInfo.KeyFrame startKeyFrame = animation.keyFrames.get(0);
        AnimationInfo.KeyFrame nextKeyFrame = animation.keyFrames.get(startKeyFrame.nextFrameIndex);

        animationPlayInfo.currentFrameIndex = 0;
        animationPlayInfo.currentFrameStartTime = startKeyFrame.time;
        animationPlayInfo.nextFrameIndex = startKeyFrame.nextFrameIndex;
        animationPlayInfo.nextFrameStartTime = nextKeyFrame.time;

        animationPlayInfos.put(animationName, animationPlayInfo);
    }

    public boolean stopAnimation(String animationName) {
        if (!animationPlayInfos.containsKey(animationName)) {
            return false;
        }

        List<String> modelNames = animInfo.getAnimations().get(animationName).models;
        modelNames.forEach(modelName -> {
            AnimationInfo.InitialProperties initialProperties = animInfo.getModels().get(modelName).initialProperties;
            MttModel3D model = models.get(modelName);
            this.applyInitialProperties(model, initialProperties);
        });

        animationPlayInfos.remove(animationName);

        return true;
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

    public void update() {
        animationPlayInfos.forEach((animationName, animationPlayInfo) -> {
            AnimationInfo.Animation animation = animInfo.getAnimations().get(animationName);

            float curTime = (float) glfwGetTime();
            if (animationPlayInfo.playing) {
                float timeElapsed = curTime - animationPlayInfo.lastTime;
                animationPlayInfo.accumulateTime += timeElapsed;

                //Get displacement per second
                float frameInterval = animationPlayInfo.nextFrameStartTime - animationPlayInfo.currentFrameStartTime;

                AnimationInfo.KeyFrame currentKeyFrame = animation.keyFrames.get(animationPlayInfo.currentFrameIndex);
                var translationPerSecond = new Vector3f(currentKeyFrame.displacement.translation).div(frameInterval);
                var rotationPerSecond = new Vector3f(currentKeyFrame.displacement.rotation).div(frameInterval);


            }
            if (animationPlayInfo.accumulateTime > animationPlayInfo.nextFrameStartTime) {
                AnimationInfo.KeyFrame currentKeyFrame = animation.keyFrames.get(animationPlayInfo.nextFrameIndex);
                AnimationInfo.KeyFrame nextKeyFrame = animation.keyFrames.get(currentKeyFrame.nextFrameIndex);

                animationPlayInfo.currentFrameIndex = animationPlayInfo.nextFrameIndex;
                animationPlayInfo.nextFrameIndex = currentKeyFrame.nextFrameIndex;
                animationPlayInfo.nextFrameStartTime = nextKeyFrame.time;
            }

            animationPlayInfo.lastTime = curTime;
        });
    }

    public Map<String, MttModel3D> getModels() {
        return new HashMap<>(models);
    }

    public Map<String, MttModel3DSet> getModelSets() {
        return new HashMap<>(modelSets);
    }
}
