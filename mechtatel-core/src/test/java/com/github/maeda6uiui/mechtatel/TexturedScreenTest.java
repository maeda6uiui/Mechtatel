package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.*;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class TexturedScreenTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(TexturedScreenTest.class);

    public TexturedScreenTest(MttSettings settings) {
        super(settings);
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
    private MttModel secondaryCube;
    private Vector3f secondaryCameraPosition;

    @Override
    public void init(MttWindow window) {
        var primaryScreenCreator = new ScreenCreator(window, "primary");
        primaryScreen = primaryScreenCreator.create();
        primaryScreen.setBackgroundColor(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        primaryScreen.getCamera().setEye(new Vector3f(2.0f, 2.0f, 2.0f));

        camera = new FreeCamera(primaryScreen.getCamera());

        var secondaryScreenCreator = new ScreenCreator(window, "secondary");
        secondaryScreenCreator.addPostProcessingNabor("parallel_light");
        secondaryScreenCreator.setDepthImageSize(1024, 1024);
        secondaryScreenCreator.setScreenSize(512, 512);
        secondaryScreenCreator.setShouldChangeExtentOnRecreate(false);
        secondaryScreen = secondaryScreenCreator.create();
        secondaryScreen.setBackgroundColor(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        secondaryScreen.createParallelLight();

        secondaryCameraPosition = new Vector3f(1.2f, 1.2f, 1.2f);
        secondaryScreen.getCamera().setEye(secondaryCameraPosition);

        var drawPath = new DrawPath(window);
        drawPath.addToScreenDrawOrder("secondary");
        drawPath.addToScreenDrawOrder("primary");
        drawPath.setPresentScreenName("primary");
        drawPath.apply();

        try {
            primaryCube = window.createModel(
                    "primary",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
            secondaryCube = window.createModel(
                    "secondary",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();
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

        secondaryCameraPosition = new Matrix4f().rotateY(0.01f).transformPosition(secondaryCameraPosition);
        secondaryScreen.getCamera().setEye(secondaryCameraPosition);

        MttTexture secondaryDrawResult = window.texturizeColorOfScreen("secondary", "primary");
        primaryCube.replaceTexture(0, secondaryDrawResult);
    }
}
