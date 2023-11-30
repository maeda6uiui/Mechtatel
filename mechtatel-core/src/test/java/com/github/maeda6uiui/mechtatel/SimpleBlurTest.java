package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.*;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.postprocessing.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
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
    private MttModel mainModel;
    private FreeCamera camera;

    @Override
    public void init(MttWindow window) {
        var mainScreenCreator = new ScreenCreator(window, "main");
        mainScreenCreator.addPostProcessingNabor("simple_blur");
        mainScreen = mainScreenCreator.create();

        var drawPath = new DrawPath(window);
        drawPath.addToScreenDrawOrder("main");
        drawPath.setPresentScreenName("main");
        drawPath.apply();

        try {
            mainModel = window.createModel(
                    "main",
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
    }
}
