package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.*;
import com.github.maeda6uiui.mechtatel.core.operation.BiTextureOperation;
import com.github.maeda6uiui.mechtatel.core.operation.BiTextureOperationParameters;
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
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class HeadlessModeTest2 extends MechtatelHeadless {
    private static final Logger logger = LoggerFactory.getLogger(HeadlessModeTest2.class);

    public HeadlessModeTest2(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        HeadlessModeTest2::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen skyboxScreen;
    private MttScreen mainScreen;
    private MttScreen finalScreen;
    private BiTextureOperation opStencil;
    private BiTextureOperation opAdd;
    private MttTexturedQuad2D texturedQuad;

    @Override
    public void onCreate(MttHeadlessInstance instance) {
        MttScreen defaultScreen = instance.getDefaultScreen();
        int width = defaultScreen.getScreenWidth();
        int height = defaultScreen.getScreenHeight();

        //Create screen for skybox
        skyboxScreen = instance.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setScreenWidth(width)
                        .setScreenHeight(height)
                        .setDepthImageWidth(1024)
                        .setDepthImageHeight(1024)
                        .setSamplerAddressMode(SamplerAddressMode.CLAMP_TO_EDGE)
        );
        //Update near and far of the camera to draw skybox
        skyboxScreen.getCamera().setZNear(500.0f);
        skyboxScreen.getCamera().setZFar(2000.0f);

        //Create main screen
        mainScreen = instance.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setScreenWidth(width)
                        .setScreenHeight(height)
        );

        //Create final screen
        finalScreen = instance.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setScreenWidth(width)
                        .setScreenHeight(height)
        );

        try {
            //Create a textured quad to render to final screen
            //Texture specified here will be replaced later
            texturedQuad = finalScreen.createTexturedQuad2D(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Texture/checker.png")),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f
            );

            //Create a model for skybox
            MttModel skyboxModel = skyboxScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Skybox/skybox.obj")));
            //Load and apply skybox textures to the model
            new SkyboxTextureCreator(
                    skyboxScreen,
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Skybox/Hill")),
                    "png",
                    false
            ).apply(skyboxModel);

            //Create a sample model to draw on main screen
            mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj")));
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            instance.close();

            return;
        }

        //Draw axes
        mainScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();

        //Create texture operations
        this.createTextureOperations();
    }

    @Override
    public void onUpdate(MttHeadlessInstance instance) {
        //Draw skybox
        skyboxScreen.draw();
        //Draw objects on main screen
        mainScreen.draw();

        //Create a stencil
        //Areas occupied by the objects drawn on the main screen are masked out as 0
        opStencil.run();

        //Add content of the main screen to the stencil
        opAdd.run();

        //Render to the final screen
        finalScreen.draw();

        //Take a screenshot
        try {
            finalScreen.save(ScreenImageType.COLOR, PixelFormat.BGRA, Paths.get("./screenshot.png"));
        } catch (IOException e) {
            logger.error("Error", e);
        }

        instance.close();
    }

    private void createTextureOperations() {
        //Clean up texture operations if there is any
        if (opStencil != null) {
            opStencil.cleanup();
            opAdd.cleanup();
        }

        //Create stencil from main screen and skybox screen
        //Mask out the areas covered with main objects from skybox rendering
        MttTexture skyboxColorTexture = skyboxScreen.texturize(ScreenImageType.COLOR, finalScreen);
        MttTexture mainStencilTexture = mainScreen.texturize(ScreenImageType.STENCIL, finalScreen);

        opStencil = finalScreen.createBiTextureOperation(
                Arrays.asList(skyboxColorTexture, mainStencilTexture),
                new ArrayList<>(),
                true
        );

        var texOpStencilParams = new BiTextureOperationParameters();
        texOpStencilParams.setOperationType(BiTextureOperationParameters.OperationType.MUL);
        opStencil.setParameters(texOpStencilParams);

        //Add rendering result of main screen to the stencil
        MttTexture stencilTexture = opStencil.getResultTexture();
        MttTexture mainColorTexture = mainScreen.texturize(ScreenImageType.COLOR, finalScreen);

        opAdd = finalScreen.createBiTextureOperation(
                Arrays.asList(stencilTexture, mainColorTexture),
                new ArrayList<>(),
                true
        );

        var texOpAddParams = new BiTextureOperationParameters();
        texOpAddParams.setOperationType(BiTextureOperationParameters.OperationType.ADD);
        opAdd.setParameters(texOpAddParams);

        //Set result texture of add operation as final output
        texturedQuad.replaceTexture(opAdd.getResultTexture());
    }
}
