package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.Spotlight;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class SpotlightTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(SpotlightTest.class);

    public SpotlightTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        SpotlightTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private FreeCamera camera;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setPostProcessingNaborNames(List.of("pp.spotlight"))
        );

        PostProcessingProperties ppProp = mainScreen.getPostProcessingProperties();
        ppProp.spotlightAmbientColor = new Vector3f(0.0f, 0.0f, 0.0f);

        Spotlight lightR = ppProp.createSpotlight();
        lightR.setPosition(new Vector3f(5.0f, 5.0f, 5.0f));
        lightR.setCenter(new Vector3f(0.0f));
        lightR.calcDirectionFromPositionAndCenter();
        lightR.setDiffuseColor(new Vector3f(1.0f, 0.0f, 0.0f));
        lightR.setSpecularClampMax(new Vector3f(0.0f));

        Spotlight lightG = ppProp.createSpotlight();
        lightG.setPosition(new Vector3f(-5.0f, 5.0f, 5.0f));
        lightG.setCenter(new Vector3f(0.0f));
        lightG.calcDirectionFromPositionAndCenter();
        lightG.setDiffuseColor(new Vector3f(0.0f, 1.0f, 0.0f));
        lightG.setSpecularClampMax(new Vector3f(0.0f));

        Spotlight lightB = ppProp.createSpotlight();
        lightB.setPosition(new Vector3f(0.0f, 5.0f, -5.0f));
        lightB.setCenter(new Vector3f(0.0f));
        lightB.calcDirectionFromPositionAndCenter();
        lightB.setDiffuseColor(new Vector3f(0.0f, 0.0f, 1.0f));
        lightB.setSpecularClampMax(new Vector3f(0.0f));

        MttModel cube;
        try {
            mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Plane/plane.obj"));
            cube = mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj"));
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        cube.rescale(new Vector3f(0.5f));
        cube.translate(new Vector3f(0.0f, 1.0f, 0.0f));

        MttModel cubeR = mainScreen.duplicateModel(cube);
        cubeR.rescale(new Vector3f(0.5f));
        cubeR.translate(new Vector3f(-5.0f, 1.0f, -5.0f));

        MttModel cubeG = mainScreen.duplicateModel(cube);
        cubeG.rescale(new Vector3f(0.5f));
        cubeG.translate(new Vector3f(5.0f, 1.0f, -5.0f));

        MttModel cubeB = mainScreen.duplicateModel(cube);
        cubeB.rescale(new Vector3f(0.5f));
        cubeB.translate(new Vector3f(0.0f, 1.0f, 7.0f));

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

        mainScreen.draw();
        window.present(mainScreen);
    }
}
