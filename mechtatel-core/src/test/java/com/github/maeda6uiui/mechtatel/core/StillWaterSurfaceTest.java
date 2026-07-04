package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.postprocessing.water.StillWaterSurface;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class StillWaterSurfaceTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(StillWaterSurfaceTest.class);

    public StillWaterSurfaceTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        StillWaterSurfaceTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private FreeCamera camera;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setPostProcessingNaborNames(List.of("pp.still_water_surface"))
        );

        //It will generate undesirable artifacts if distortion is too strong.
        PostProcessingProperties ppProp = mainScreen.getPostProcessingProperties();
        StillWaterSurface water = ppProp.stillWaterSurface;
        water.setWaterLevel(0.5f);
        water.setShallowColor(new Vector3f(0.3f, 0.7f, 0.75f));
        water.setDeepColor(new Vector3f(0.0f, 0.1f, 0.25f));
        water.setWaveAmplitude(0.15f);
        water.setWaveFrequency(1.2f);
        water.setWaveSpeed(1.5f);
        water.setDistortionStrength(0.01f);
        water.setAbsorptionCoefficient(0.2f);
        water.setSpecularStrength(0.8f);
        water.setSunDirection(new Vector3f(-0.4f, -1.0f, -0.3f).normalize());
        water.setHorizonColor(new Vector3f(0.7f, 0.8f, 0.85f));
        water.setZenithColor(new Vector3f(0.25f, 0.45f, 0.75f));

        MttModel floor;
        MttModel cube;
        try {
            floor = mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Plane/plane.obj"));
            cube = mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj"));

            MttTexture checker = mainScreen.createTexture(Paths.get("./Mechtatel/Standard/Texture/checker.png"), true);
            floor.replaceTexture(0, checker);
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        floor.translate(new Vector3f(0.0f, -2.0f, 0.0f));

        cube.rescale(new Vector3f(0.5f));
        cube.translate(new Vector3f(0.0f, -1.0f, 0.0f));

        MttModel cube2 = mainScreen.duplicateModel(cube);
        cube2.translate(new Vector3f(-3.0f, 0.0f, -3.0f));

        MttModel cube3 = mainScreen.duplicateModel(cube);
        cube3.translate(new Vector3f(3.0f, 0.0f, -2.0f));

        MttModel cube4 = mainScreen.duplicateModel(cube);
        cube4.translate(new Vector3f(-1.0f, 0.0f, 3.0f));

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
