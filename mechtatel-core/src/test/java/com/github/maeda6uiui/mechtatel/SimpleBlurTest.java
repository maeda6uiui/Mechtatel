package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.postprocessing.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;

public class SimpleBlurTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(SimpleBlurTest.class);

    public SimpleBlurTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        SimpleBlurTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private FreeCamera camera;

    @Override
    public void init(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setPpNaborNames(List.of("simple_blur"))
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

        camera = new FreeCamera(mainScreen.getCamera());
    }

    @Override
    public void update(MttWindow window) {
        var simpleBlurInfo = new SimpleBlurInfo();
        simpleBlurInfo.setTextureWidth(mainScreen.getScreenWidth());
        simpleBlurInfo.setTextureHeight(mainScreen.getScreenHeight());
        simpleBlurInfo.setBlurSize(10);
        simpleBlurInfo.setStride(1);
        mainScreen.setSimpleBlurInfo(simpleBlurInfo);

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

        window.present(mainScreen);
    }
}
