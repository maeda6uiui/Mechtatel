package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class TexturedScreenTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(TexturedScreenTest.class);

    public TexturedScreenTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        TexturedScreenTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen primaryScreen;
    private MttModel primaryCube;
    private FreeCamera camera;

    private MttScreen secondaryScreen;
    private Vector3f secondaryCameraPosition;

    @Override
    public void onCreate(MttWindow window) {
        primaryScreen = window.createScreen(new MttScreen.MttScreenCreateInfo());
        primaryScreen.setBackgroundColor(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        primaryScreen.getCamera().setEye(new Vector3f(2.0f, 2.0f, 2.0f));
        camera = new FreeCamera(primaryScreen.getCamera());

        secondaryScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setPostProcessingNaborNames(List.of("parallel_light"))
                        .setDepthImageWidth(1024)
                        .setDepthImageHeight(1024)
                        .setScreenWidth(512)
                        .setScreenHeight(512)
                        .setShouldChangeExtentOnRecreate(false)
        );
        secondaryScreen.setBackgroundColor(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        secondaryCameraPosition = new Vector3f(1.2f, 1.2f, 1.2f);
        secondaryScreen.getCamera().setEye(secondaryCameraPosition);

        PostProcessingProperties ppProperties = secondaryScreen.getPostProcessingProperties();
        ppProperties.createParallelLight();

        try {
            Path modelFile = Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj");
            primaryCube = primaryScreen.createModel(modelFile);
            secondaryScreen.createModel(modelFile);
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();
        }

        MttTexture secondaryDrawResult = secondaryScreen.texturize(ScreenImageType.COLOR, primaryScreen);
        primaryCube.replaceTexture(0, secondaryDrawResult);
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

        secondaryCameraPosition = new Matrix4f().rotateY(0.01f).transformPosition(secondaryCameraPosition);
        secondaryScreen.getCamera().setEye(secondaryCameraPosition);

        secondaryScreen.draw();
        primaryScreen.draw();
        window.present(primaryScreen);
    }
}
