package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.fseffect.GaussianBlurInfo;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class GaussianBlurTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(GaussianBlurTest.class);

    public GaussianBlurTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        GaussianBlurTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private FreeCamera camera;

    private float standardDeviation;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setFullScreenEffectNaborNames(List.of("gaussian_blur"))
        );

        try {
            mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        mainScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();

        camera = new FreeCamera(mainScreen.getCamera());

        standardDeviation = 1.0f;
    }

    @Override
    public void onUpdate(MttWindow window) {
        if (window.getKeyboardPressingCount(KeyCode.KEY_1) == 1) {
            standardDeviation += 1.0f;
        } else if (window.getKeyboardPressingCount(KeyCode.KEY_2) == 1) {
            standardDeviation -= 1.0f;
        }
        if (standardDeviation < 1.0f) {
            standardDeviation = 1.0f;
        }

        var gaussianBlurInfo = new GaussianBlurInfo(8, standardDeviation);
        gaussianBlurInfo.setTextureSize(new Vector2i(mainScreen.getScreenWidth(), mainScreen.getScreenHeight()));
        mainScreen.setGaussianBlurInfo(gaussianBlurInfo);

        camera.translate(
                window.getKeyboardPressingCount(KeyCode.W),
                window.getKeyboardPressingCount(KeyCode.S),
                window.getKeyboardPressingCount(KeyCode.A),
                window.getKeyboardPressingCount(KeyCode.D)
        );
        camera.rotate(
                window.getKeyboardPressingCount(KeyCode.UP),
                window.getKeyboardPressingCount(KeyCode.DOWN),
                window.getKeyboardPressingCount(KeyCode.LEFT),
                window.getKeyboardPressingCount(KeyCode.RIGHT)
        );

        mainScreen.draw();
        window.present(mainScreen);
    }
}
