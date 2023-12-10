package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.*;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
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
        this.run();
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
    private TextureOperation opMergeByDepth;
    private MttTexturedQuad2D texturedQuad;
    private FreeCamera camera;

    @Override
    public void init(MttWindow window) {
        skyboxScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setDepthImageWidth(1024)
                        .setDepthImageHeight(1024)
                        .setSamplerAddressMode(SamplerAddressMode.CLAMP_TO_EDGE)
        );
        skyboxScreen.getCamera().setZNear(500.0f);
        skyboxScreen.getCamera().setZFar(2000.0f);

        mainScreen = window.createScreen(new MttScreen.MttScreenCreateInfo());
        finalScreen = window.createScreen(new MttScreen.MttScreenCreateInfo());

        try {
            texturedQuad = finalScreen.createTexturedQuad2D(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Texture/checker.png")),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f
            );

            MttModel skyboxModel = skyboxScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Skybox/skybox.obj")));
            mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj")));

            new SkyboxTextureCreator(
                    skyboxScreen,
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Skybox/Hill")),
                    "png",
                    false
            ).apply(skyboxModel);
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        mainScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();

        MttTexture skyboxColorTexture = skyboxScreen.texturize(ScreenImageType.COLOR, finalScreen);
        MttTexture skyboxDepthTexture = skyboxScreen.texturize(ScreenImageType.DEPTH, finalScreen);
        MttTexture mainColorTexture = mainScreen.texturize(ScreenImageType.COLOR, finalScreen);
        MttTexture mainDepthTexture = mainScreen.texturize(ScreenImageType.DEPTH, finalScreen);

        var textureOperationParameters = new TextureOperationParameters();
        textureOperationParameters.setOperationType(TextureOperationParameters.TEXTURE_OPERATION_MERGE_BY_DEPTH);
        textureOperationParameters.setFirstTextureFixedDepth(0.99999f);

        opMergeByDepth = finalScreen.createTextureOperation(
                skyboxColorTexture,
                skyboxDepthTexture,
                mainColorTexture,
                mainDepthTexture
        );
        opMergeByDepth.setParameters(textureOperationParameters);
        texturedQuad.replaceTexture(opMergeByDepth.getResultTexture());

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

        skyboxScreen.draw();
        mainScreen.draw();
        opMergeByDepth.run();
        finalScreen.draw();
        window.present(finalScreen);
    }
}
