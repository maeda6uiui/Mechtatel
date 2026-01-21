package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.CameraMode;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class CameraModeTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(CameraModeTest.class);

    public CameraModeTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        CameraModeTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private FreeCamera camera;

    @Override
    public void onCreate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();
        camera = new FreeCamera(defaultScreen.getCamera());

        MttModel srcCube;
        try {
            srcCube = defaultScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj"));
            srcCube.setVisible(false);
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        float z = -25.0f;
        for (int i = 0; i <= 10; i++) {
            float x = -25.0f;
            for (int j = 0; j <= 10; j++) {
                MttModel cube = defaultScreen.duplicateModel(srcCube);
                cube.translate(new Vector3f(x, 0.0f, z));

                x += 5.0f;
            }
            z += 5.0f;
        }
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

        MttScreen defaultScreen = window.getDefaultScreen();
        if (window.getKeyboardPressingCount(KeyCode.F1) == 1) {
            defaultScreen.getCamera().setCameraMode(CameraMode.ORTHOGRAPHIC);
        } else if (window.getKeyboardPressingCount(KeyCode.F2) == 1) {
            defaultScreen.getCamera().setCameraMode(CameraMode.PERSPECTIVE);
        }

        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
