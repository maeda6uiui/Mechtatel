package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.animation.AnimationInfo;
import com.github.maeda6uiui.mechtatel.core.animation.MttAnimation;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class AnimationTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(AnimationTest.class);

    public AnimationTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        AnimationTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttAnimation animation;
    private FreeCamera camera;

    @Override
    public void init(MttWindow window) {
        try {
            var animationInfo = new AnimationInfo(
                    Objects.requireNonNull(
                            this.getClass().getResource("/Standard/Model/Cube/sample_animations.json")));
            animation = window.createAnimation("cubes", "default", animationInfo);
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        System.out.println("model_names=");
        animation.getModels().keySet().forEach(System.out::println);

        System.out.println("animation_names=");
        animation.getModelSets().keySet().forEach(System.out::println);

        MttScreen defaultScreen = window.getScreen("default");
        defaultScreen.getCamera().setEye(new Vector3f(10.0f, 10.0f, 10.0f));

        window.createLineSet().addAxes(10.0f).createBuffer();

        camera = new FreeCamera(defaultScreen.getCamera());
    }

    @Override
    public void update(MttWindow window) {
        camera.translate(
                window.getKeyboardPressingCount("W"),
                window.getKeyboardPressingCount("S"),
                window.getKeyboardPressingCount("A"),
                window.getKeyboardPressingCount("D")
        );
        camera.rotate(
                window.getKeyboardPressingCount("UP"),
                window.getKeyboardPressingCount("DOWN"),
                window.getKeyboardPressingCount("LEFT"),
                window.getKeyboardPressingCount("RIGHT")
        );

        if (window.getKeyboardPressingCount("1") == 1) {
            animation.startAnimation("up_and_down_with_rotation");
        } else if (window.getKeyboardPressingCount("2") == 1) {
            animation.stopAnimation("up_and_down_with_rotation");
        } else if (window.getKeyboardPressingCount("3") == 1) {
            animation.startAnimation("right_and_left_with_rotation");
        } else if (window.getKeyboardPressingCount("4") == 1) {
            animation.stopAnimation("right_and_left_with_rotation");
        } else if (window.getKeyboardPressingCount("5") == 1) {
            animation.stopAllAnimations();
        }
    }
}
