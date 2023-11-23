package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.animation.AnimationInfo;
import com.github.maeda6uiui.mechtatel.core.animation.MttAnimation;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector3f;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class AnimationTest extends Mechtatel {
    public AnimationTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        AnimationTest::new,
                        () -> {
                            System.out.println("Failed to load settings");
                        }
                );
    }

    private MttAnimation animation;
    private FreeCamera camera;

    @Override
    public void init() {
        try {
            var animationInfo = new AnimationInfo(
                    Objects.requireNonNull(
                            this.getClass().getResource("/Standard/Model/Cube/sample_animations.json")));
            animation = this.createAnimation("cubes", "default", animationInfo);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            this.closeWindow();

            return;
        }

        System.out.println("model_names=");
        animation.getModels().keySet().forEach(System.out::println);

        System.out.println("animation_names=");
        animation.getModelSets().keySet().forEach(System.out::println);

        MttScreen defaultScreen = this.getScreen("default");
        defaultScreen.getCamera().setEye(new Vector3f(10.0f, 10.0f, 10.0f));

        this.createAxesLineSet(10.0f);

        camera = new FreeCamera(defaultScreen.getCamera());
    }

    @Override
    public void update() {
        camera.translate(
                this.getKeyboardPressingCount("W"),
                this.getKeyboardPressingCount("S"),
                this.getKeyboardPressingCount("A"),
                this.getKeyboardPressingCount("D")
        );
        camera.rotate(
                this.getKeyboardPressingCount("UP"),
                this.getKeyboardPressingCount("DOWN"),
                this.getKeyboardPressingCount("LEFT"),
                this.getKeyboardPressingCount("RIGHT")
        );

        if (this.getKeyboardPressingCount("1") == 1) {
            animation.startAnimation("up_and_down_with_rotation");
        } else if (this.getKeyboardPressingCount("2") == 1) {
            animation.stopAnimation("up_and_down_with_rotation");
        } else if (this.getKeyboardPressingCount("3") == 1) {
            animation.startAnimation("right_and_left_with_rotation");
        } else if (this.getKeyboardPressingCount("4") == 1) {
            animation.stopAnimation("right_and_left_with_rotation");
        } else if (this.getKeyboardPressingCount("5") == 1) {
            animation.stopAllAnimations();
        }
    }
}
