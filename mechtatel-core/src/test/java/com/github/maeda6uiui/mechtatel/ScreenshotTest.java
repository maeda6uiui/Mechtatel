package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.*;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.component.MttSphere;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class ScreenshotTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(ScreenshotTest.class);

    public ScreenshotTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ScreenshotTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private FreeCamera camera;

    private MttModel plane;
    private MttModel cube;
    private MttSphere sphere;

    @Override
    public void init(MttWindow window) {
        var screenCreator = new ScreenCreator(window, "main");
        screenCreator.addPostProcessingNabor("parallel_light");
        screenCreator.addPostProcessingNabor("fog");
        screenCreator.setUseShadowMapping(true);
        var mainScreen = screenCreator.create();

        mainScreen.createParallelLight();
        mainScreen.getFog().setStart(10.0f);
        mainScreen.getFog().setEnd(20.0f);

        camera = new FreeCamera(mainScreen.getCamera());

        var drawPath = new DrawPath(window);
        drawPath.addToScreenDrawOrder("main");
        drawPath.setPresentScreenName("main");
        drawPath.apply();

        try {
            plane = window.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Plane/plane.obj"))
            );
            cube = window.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        cube.translate(new Vector3f(0.0f, 1.0f, 0.0f));

        sphere = window.createSphere(
                new Vector3f(5.0f, 2.0f, 0.0f),
                2.0f, 32, 32, new Vector4f(1.0f, 0.0f, 1.0f, 1.0f)
        );
    }

    @Override
    public void dispose(MttWindow window) {

    }

    @Override
    public void reshape(MttWindow window, int width, int height) {

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
    }

    @Override
    public void postPresent(MttWindow window) {
        if (window.getKeyboardPressingCount("ENTER") == 1) {
            try {
                window.saveScreenshot("main", "bgra", "screenshot.png");
            } catch (IOException e) {
                logger.error("Error", e);
                window.close();
            }
        }
    }
}
