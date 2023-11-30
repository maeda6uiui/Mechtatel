package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.*;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.texture.TextureOperationParameters;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class SkyboxTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(SkyboxTest.class);

    public SkyboxTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        SkyboxTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen skyboxScreen;
    private MttScreen mainScreen;
    private MttScreen finalScreen;

    private MttTexture skyboxColorTexture;
    private MttTexture skyboxDepthTexture;
    private MttTexture mainColorTexture;
    private MttTexture mainDepthTexture;
    private MttTexture finalTexture;

    private MttTexturedQuad2D texturedQuad;

    private MttModel skyboxModel;
    private MttModel mainModel;

    private FreeCamera camera;

    @Override
    public void init(MttWindow window) {
        var skyboxScreenCreator = new ScreenCreator(window, "skybox");
        skyboxScreenCreator.setDepthImageSize(1024, 1024);
        skyboxScreenCreator.setSamplerAddressMode(SamplerAddressMode.CLAMP_TO_EDGE);
        skyboxScreen = skyboxScreenCreator.create();
        skyboxScreen.getCamera().setZNear(500.0f);
        skyboxScreen.getCamera().setZFar(2000.0f);

        var mainScreenCreator = new ScreenCreator(window, "main");
        mainScreen = mainScreenCreator.create();

        var finalScreenCreator = new ScreenCreator(window, "final");
        finalScreen = finalScreenCreator.create();

        var drawPath = new DrawPath(window);
        drawPath.addToScreenDrawOrder("skybox");
        drawPath.addToScreenDrawOrder("main");
        drawPath.addToTextureOperationOrder("merge_by_depth");
        drawPath.addToDeferredScreenDrawOrder("final");
        drawPath.setPresentScreenName("final");
        drawPath.apply();

        try {
            texturedQuad = window.createTexturedQuad2D(
                    "final",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Texture/checker.png")),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f
            );

            skyboxModel = window.createModel(
                    "skybox",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Skybox/skybox.obj"))
            );
            mainModel = window.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );

            var skyboxTextureCreator = new SkyboxTextureCreator(
                    window,
                    "skybox",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Skybox/Hill")),
                    "png",
                    false);
            skyboxTextureCreator.apply(skyboxModel);
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        window.createLineSet().addPositiveAxes(10.0f).createBuffer();

        camera = new FreeCamera(mainScreen.getCamera());
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
        skyboxScreen.syncCamera(mainScreen.getCamera());

        skyboxColorTexture = window.texturizeColorOfScreen("skybox", "final");
        skyboxDepthTexture = window.texturizeDepthOfScreen("skybox", "final");
        mainColorTexture = window.texturizeColorOfScreen("main", "final");
        mainDepthTexture = window.texturizeDepthOfScreen("main", "final");

        var textureOperationParameters = new TextureOperationParameters();
        textureOperationParameters.setOperationType(TextureOperationParameters.TEXTURE_OPERATION_MERGE_BY_DEPTH);
        textureOperationParameters.setFirstTextureFixedDepth(0.99999f);

        finalTexture = window.createTextureOperation(
                "merge_by_depth",
                skyboxColorTexture,
                mainColorTexture,
                skyboxDepthTexture,
                mainDepthTexture,
                "final",
                textureOperationParameters
        );
        texturedQuad.replaceTexture(finalTexture);
    }

    @Override
    public void postPresent(MttWindow window) {
        if (window.getKeyboardPressingCount("ENTER") == 1) {
            try {
                window.saveScreenshot("final", "bgra", "screenshot.jpg");
            } catch (IOException e) {
                logger.error("Error", e);
                window.close();

                return;
            }
        }

        skyboxColorTexture.cleanup();
        skyboxDepthTexture.cleanup();
        mainColorTexture.cleanup();
        mainDepthTexture.cleanup();
        finalTexture.cleanup();
    }
}
