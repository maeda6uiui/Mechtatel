package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttSphere;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Arrays;

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
                        .setPostProcessingNaborNames(Arrays.asList("pp.parallel_light", "pp.fog"))
        );

        PostProcessingProperties ppProperties = mainScreen.getPostProcessingProperties();
        ppProperties.createParallelLight();
        ppProperties.fog.setStart(10.0f);
        ppProperties.fog.setEnd(20.0f);

        camera = new FreeCamera(mainScreen.getCamera());

        try {
            mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Plane/plane.obj"));

            MttModel cube = mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj"));
            cube.translate(new Vector3f(0.0f, 1.0f, 0.0f));
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        MttSphere sphere = mainScreen.createSphere(
                2.0f, 32, 32,
                new Vector4f(1.0f, 0.0f, 1.0f, 1.0f),
                false
        );
        sphere.translate(new Vector3f(5.0f, 2.0f, 0.0f));
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
