package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttCapsule;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttSphere;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

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

    private MttScreen mainScreen;
    private FreeCamera camera;

    private MttSphere filledSphere;
    private MttCapsule filledCapsule;
    private MttModel cube;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setPostProcessingNaborNames(List.of("parallel_light"))
        );

        PostProcessingProperties ppProp = mainScreen.getPostProcessingProperties();
        ppProp.createParallelLight();

        mainScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();

        filledSphere = mainScreen.createSphere(
                new Vector3f(0.0f),
                1.0f,
                16,
                16,
                new Vector4f(0.0f, 1.0f, 0.0f, 1.0f),
                true
        );
        filledSphere.translate(new Vector3f(3.0f, 0.0f, 0.0f));
        filledCapsule = mainScreen.createCapsule(
                new Vector3f(0.0f),
                1.0f,
                1.0f,
                16,
                16,
                new Vector4f(0.0f, 0.0f, 1.0f, 1.0f),
                true
        );
        filledCapsule.translate(new Vector3f(6.0f, 0.0f, 0.0f));

        try {
            cube = mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj"));
            cube.translate(new Vector3f(9.0f, 0.0f, 0.0f));
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        camera = new FreeCamera(mainScreen.getCamera());
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

        filledCapsule.rotX(0.01f);
        cube.rotY(0.01f);

        mainScreen.draw();
        window.present(mainScreen);
    }
}
