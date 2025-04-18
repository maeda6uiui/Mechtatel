package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectProperties;
import com.github.maeda6uiui.mechtatel.core.fseffect.GaussianBlurInfo;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

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

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setFullScreenEffectNaborNames(List.of("fse.gaussian_blur"))
        );

        try {
            mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Playground/playground.bd1"));
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        camera = new FreeCamera(mainScreen.getCamera());

        var gaussianBlurInfo = new GaussianBlurInfo(32, 16.0f, 0.1f);
        gaussianBlurInfo.setTextureSize(new Vector2i(mainScreen.getScreenWidth(), mainScreen.getScreenHeight()));

        FullScreenEffectProperties fseProperties = mainScreen.getFullScreenEffectProperties();
        fseProperties.gaussianBlurInfo = gaussianBlurInfo;

        logger.info(Arrays.toString(gaussianBlurInfo.getWeights()));
    }

    @Override
    public void onUpdate(MttWindow window) {
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
