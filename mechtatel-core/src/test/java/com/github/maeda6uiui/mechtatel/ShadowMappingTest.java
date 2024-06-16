package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class ShadowMappingTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(ShadowMappingTest.class);

    public ShadowMappingTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ShadowMappingTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private FreeCamera camera;

    private List<MttModel> cubes;
    private List<Vector3f> cubePositions;
    private List<Float> cubeRotations;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setUseShadowMapping(true)
                        .setPostProcessingNaborNames(Arrays.asList("parallel_light", "fog"))
        );
        mainScreen.createParallelLight();
        mainScreen.getFog().setStart(10.0f);
        mainScreen.getFog().setEnd(20.0f);

        var shadowMappingSettings = new ShadowMappingSettings();
        shadowMappingSettings.setBiasCoefficient(0.002f);
        mainScreen.setShadowMappingSettings(shadowMappingSettings);

        camera = new FreeCamera(mainScreen.getCamera());

        cubes = new ArrayList<>();
        cubePositions = new ArrayList<>();
        cubeRotations = new ArrayList<>();
        try {
            MttModel plane = mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Plane/plane.obj"))
            );
            plane.rescale(new Vector3f(2.0f, 1.0f, 2.0f));

            MttModel teapot = mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Teapot/teapot.obj"))
            );
            teapot.rescale(new Vector3f(2.0f, 2.0f, 2.0f));

            var cube = mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
            cube.translate(new Vector3f(6.0f, 2.0f, 0.0f));
            cubes.add(cube);
            cubePositions.add(new Vector3f(6.0f, 2.0f, 0.0f));
            cubeRotations.add(0.0f);
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();
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

        if (window.getKeyboardPressingCount(KeyCode.ENTER) == 1) {
            var srcCube = cubes.get(0);
            var dupCube = mainScreen.duplicateModel(srcCube);

            dupCube.translate(new Vector3f(6.0f, 2.0f, 0.0f));
            cubes.add(dupCube);
            cubePositions.add(new Vector3f(6.0f, 2.0f, 0.0f));
            cubeRotations.add(0.0f);
        }

        for (int i = 0; i < cubes.size(); i++) {
            var cube = cubes.get(i);
            var cubePosition = cubePositions.get(i);
            var cubeRotation = cubeRotations.get(i);

            //Translation
            var posRotMat = new Matrix4f().rotate((float) Math.toRadians(0.5f), 0.0f, 1.0f, 0.0f);
            var newCubePosition = posRotMat.transformPosition(cubePosition);
            cubePositions.set(i, newCubePosition);

            //Rotation
            final float CUBE_ROT_ANGLE = (float) Math.toRadians(0.5f);
            float newCubeRotation = cubeRotation + CUBE_ROT_ANGLE;
            cubeRotations.set(i, newCubeRotation);

            var transformMat = new Matrix4f()
                    .translate(newCubePosition)
                    .rotateX(newCubeRotation)
                    .rotateY(newCubeRotation)
                    .rotateZ(newCubeRotation);
            cube.setMat(transformMat);
        }

        mainScreen.draw();
        window.present(mainScreen);
    }
}
