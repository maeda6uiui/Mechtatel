package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.CameraMode;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class CameraModeTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(CameraModeTest.class);

    public CameraModeTest(MttSettings settings) {
        super(settings);
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
    public void init(MttWindow window) {
        MttScreen defaultScreen = window.getScreen("default");
        camera = new FreeCamera(defaultScreen.getCamera());

        MttModel srcCube;
        try {
            srcCube = window.createModel(
                    "default",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
            srcCube.setVisible(false);
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        float z = -25.0f;
        for (int i = 0; i <= 10; i++) {
            float x = -25.0f;
            for (int j = 0; j <= 10; j++) {
                MttModel cube = window.duplicateModel(srcCube);
                cube.translate(new Vector3f(x, 0.0f, z));

                x += 5.0f;
            }
            z += 5.0f;
        }
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

        MttScreen defaultScreen = window.getScreen("default");
        if (window.getKeyboardPressingCount("F1") == 1) {
            defaultScreen.getCamera().setCameraMode(CameraMode.ORTHOGRAPHIC);
        } else if (window.getKeyboardPressingCount("F2") == 1) {
            defaultScreen.getCamera().setCameraMode(CameraMode.PERSPECTIVE);
        }
    }
}
