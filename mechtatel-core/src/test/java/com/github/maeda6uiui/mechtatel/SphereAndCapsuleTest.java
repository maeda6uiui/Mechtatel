package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SphereAndCapsuleTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(SphereAndCapsuleTest.class);

    public SphereAndCapsuleTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        SphereAndCapsuleTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private FreeCamera camera;

    @Override
    public void onCreate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();

        defaultScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();
        defaultScreen.createSphere(
                new Vector3f(0.0f, 0.0f, 0.0f),
                1.0f,
                16,
                16,
                new Vector4f(1.0f),
                false
        );
        defaultScreen.createCapsule(
                new Vector3f(3.0f, 0.0f, 0.0f),
                1.0f,
                1.0f,
                16,
                16,
                new Vector4f(1.0f)
        );
        defaultScreen.createSphere(
                new Vector3f(6.0f, 0.0f, 0.0f),
                1.0f,
                16,
                16,
                new Vector4f(1.0f),
                true
        );

        camera = new FreeCamera(defaultScreen.getCamera());
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
        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
