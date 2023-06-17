package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.CameraMode;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel3D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector3f;

import java.io.IOException;

public class CameraModeTest extends Mechtatel {
    public CameraModeTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = MttSettings.load("./Mechtatel/Setting/settings.json");
            settings.systemSettings.gatewayServer.enabled = true;
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new CameraModeTest(settings);
    }

    private FreeCamera camera;

    @Override
    public void init() {
        MttScreen defaultScreen = this.getScreen("default");
        camera = new FreeCamera(defaultScreen.getCamera());

        MttModel3D srcCube;
        try {
            srcCube = this.createModel3D(
                    "default", "./Mechtatel/Standard/Model/Cube/cube.obj");
            srcCube.setVisible(false);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        float z = -25.0f;
        for (int i = 0; i <= 10; i++) {
            float x = -25.0f;
            for (int j = 0; j <= 10; j++) {
                MttModel3D cube = this.duplicateModel3D(srcCube);
                cube.translate(new Vector3f(x, 0.0f, z));

                x += 5.0f;
            }
            z += 5.0f;
        }
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

        MttScreen defaultScreen = this.getScreen("default");
        if (this.getKeyboardPressingCount("F1") == 1) {
            defaultScreen.getCamera().setCameraMode(CameraMode.ORTHOGRAPHIC);
        } else if (this.getKeyboardPressingCount("F2") == 1) {
            defaultScreen.getCamera().setCameraMode(CameraMode.PERSPECTIVE);
        }
    }
}
