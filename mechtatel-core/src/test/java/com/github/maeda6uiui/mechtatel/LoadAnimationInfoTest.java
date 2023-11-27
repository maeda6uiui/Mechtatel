package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.animation.AnimationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class LoadAnimationInfoTest {
    private static final Logger logger = LoggerFactory.getLogger(LoadAnimationInfoTest.class);

    public static void main(String[] args) {
        try {
            var animInfo = new AnimationInfo(
                    Objects.requireNonNull(LoadAnimationInfoTest.class.getResource(
                            "/Standard/Model/Cube/sample_animations.json")));

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
                    System.out.printf("%d: %s\n", kk, vv.displacement.translation);
                });
            });
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
        }
    }
}
