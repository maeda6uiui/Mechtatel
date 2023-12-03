package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.screen.animation.AnimationInfo;
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

            logger.info("Asset name: {}", animInfo.getName());
            logger.info("Models:");
            animInfo.getModels().forEach((k, v) -> logger.info("- {}: {}", k, v.filename));
            logger.info("Animations:");
            animInfo.getAnimations().forEach((k, v) -> {
                logger.info("===== {} =====", k);
                v.keyFrames.forEach((kk, vv) -> logger.info("- {}: {}", kk, vv.displacement.translation));
            });
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
        }
    }
}
