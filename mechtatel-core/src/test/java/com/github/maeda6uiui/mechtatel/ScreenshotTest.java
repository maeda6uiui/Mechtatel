package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Arrays;
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

    private MttScreen mainScreen;
    private FreeCamera camera;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setUseShadowMapping(true)
                        .setPpNaborNames(Arrays.asList("parallel_light", "fog"))
        );
        mainScreen.createParallelLight();
        mainScreen.getFog().setStart(10.0f);
        mainScreen.getFog().setEnd(20.0f);

        camera = new FreeCamera(mainScreen.getCamera());

        try {
            mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Plane/plane.obj"))
            );

            MttModel cube = mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
            cube.translate(new Vector3f(0.0f, 1.0f, 0.0f));
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        mainScreen.createSphere(
                new Vector3f(5.0f, 2.0f, 0.0f),
                2.0f, 32, 32,
                new Vector4f(1.0f, 0.0f, 1.0f, 1.0f)
        );
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

        if (window.getKeyboardPressingCount(KeyCode.ENTER) == 1) {
            try {
                mainScreen.save(ScreenImageType.COLOR, PixelFormat.BGRA, Paths.get("./screenshot.png"));
            } catch (IOException e) {
                logger.error("Error", e);
                window.close();
            }
        }
    }
}
