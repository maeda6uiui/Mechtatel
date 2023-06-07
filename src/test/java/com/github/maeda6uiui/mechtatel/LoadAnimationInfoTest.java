package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.animation.AnimationInfo;

import java.io.IOException;

public class LoadAnimationInfoTest {
    public static void main(String[] args) {
        try {
            var animInfo = new AnimationInfo("./Mechtatel/Standard/Model/Cube/sample_animations.json");

            System.out.println("asset_name=");
            System.out.println(animInfo.getName());

            System.out.println("models=");
            var models = animInfo.getModels();
            models.forEach((k, v) -> {
                System.out.printf("%s: %s\n", k, v.filename);
            });

            System.out.println("animations=");
            var animations = animInfo.getAnimations();
            animations.forEach((k, v) -> {
                System.out.printf("--- %s ---\n", k);
                v.keyFrames.forEach((kk, vv) -> {
                    if (vv.displacement != null) {
                        System.out.printf("%d: %s\n", kk, vv.displacement.translation);
                    } else if (vv.revertDisplacement != null) {
                        System.out.printf("%d: %d\n", kk, vv.revertDisplacement.frameIndex);
                    }
                });
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
