package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class ParallelLightTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(ParallelLightTest.class);

    public ParallelLightTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ParallelLightTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private MttModel cube;
    private MttModel dupCube;
    private FreeCamera camera;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setPostProcessingNaborNames(List.of("pp.parallel_light"))
        );

        PostProcessingProperties ppProp = mainScreen.getPostProcessingProperties();
        ppProp.createParallelLight();

        try {
            cube = mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj"));
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        dupCube = mainScreen.duplicateModel(cube);
        dupCube.translate(new Vector3f(3.0f, 0.0f, 0.0f));

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

        cube.rotY(0.01f);
        dupCube.rotY(0.01f);

        mainScreen.draw();
        window.present(mainScreen);
    }
}
