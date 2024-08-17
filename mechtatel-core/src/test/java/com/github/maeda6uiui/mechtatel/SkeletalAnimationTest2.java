package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.*;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.model.MttAnimationData;
import com.github.maeda6uiui.mechtatel.core.model.MttModelData;
import com.github.maeda6uiui.mechtatel.core.model.helper.AnimationPlayMode;
import com.github.maeda6uiui.mechtatel.core.model.helper.AnimationUpdater;
import com.github.maeda6uiui.mechtatel.core.operation.TextureOperation;
import com.github.maeda6uiui.mechtatel.core.operation.TextureOperationParameters;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.postprocessing.light.ParallelLight;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.shadow.ShadowMappingSettings;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class SkeletalAnimationTest2 extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(SkeletalAnimationTest2.class);

    public SkeletalAnimationTest2(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        SkeletalAnimationTest2::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen skyboxScreen;
    private MttScreen mainScreen;
    private MttScreen finalScreen;
    private TextureOperation opStencil;
    private TextureOperation opAdd;
    private MttTexturedQuad2D texturedQuad;
    private FreeCamera camera;

    private MttModel animModel;
    private AnimationUpdater animUpdater;

    @Override
    public void onCreate(MttWindow window) {
        //Create screen for skybox
        skyboxScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setDepthImageWidth(1024)
                        .setDepthImageHeight(1024)
                        .setSamplerAddressMode(SamplerAddressMode.CLAMP_TO_EDGE)
        );
        //Update near and far of the camera to draw skybox
        skyboxScreen.getCamera().setZNear(500.0f);
        skyboxScreen.getCamera().setZFar(2000.0f);

        //Create main screen
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setUseShadowMapping(true)
                        .setPostProcessingNaborNames(Arrays.asList("parallel_light"))
        );

        //Create light
        PostProcessingProperties ppProperties = mainScreen.getPostProcessingProperties();
        ParallelLight parallelLight = ppProperties.createParallelLight();

        var lightPosition = new Vector3f(50.0f, 50.0f, 50.0f);
        var lightCenter = new Vector3f(0.0f, 0.0f, 0.0f);
        var lightDirection = new Vector3f(lightCenter).sub(lightPosition).normalize();
        parallelLight.setPosition(lightPosition);
        parallelLight.setCenter(lightCenter);
        parallelLight.setDirection(lightDirection);

        //Set shadow mapping
        var shadowMappingSettings = new ShadowMappingSettings();
        shadowMappingSettings.setBiasCoefficient(0.002f);
        mainScreen.setShadowMappingSettings(shadowMappingSettings);

        //Create final screen that will be presented to the window
        finalScreen = window.createScreen(new MttScreen.MttScreenCreateInfo());

        try {
            //Create a textured quad to render to final screen
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

            //Create a model for animation
            animModel = mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cz1/cz_1.dae")));

            //Create a model for ground
            MttModel groundModel = mainScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Plane/plane.obj")));
            groundModel.rescale(new Vector3f(0.5f, 1.0f, 0.5f));
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        //Attach first animation to the model
        MttModelData.Animation animation = animModel.getModelData().animationList.get(0);
        var animationData = new MttAnimationData(animation, 0, 0);
        animModel.setAnimationData(animationData);

        //Create helper class to update animation
        animUpdater = new AnimationUpdater(
                animationData,
                AnimationPlayMode.REPEAT,
                1.0,
                this.getSecondsPerFrame()
        );

        //Draw axes
        mainScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();

        //Camera is set on the main screen
        mainScreen.getCamera().setEye(new Vector3f(-2.0f, 2.0f, 2.0f));
        camera = new FreeCamera(mainScreen.getCamera());

        //Create texture operations
        this.createTextureOperations();
    }

    @Override
    public void onRecreate(MttWindow window, int width, int height) {
        this.createTextureOperations();
    }

    @Override
    public void onUpdate(MttWindow window) {
        //Translate and rotate camera according to key input
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
        //Synchronize the camera of the skybox screen to the one of the main screen
        skyboxScreen.syncCamera(mainScreen.getCamera());

        //Draw skybox
        skyboxScreen.draw();
        //Draw objects on main screen
        mainScreen.draw();

        //Create a stencil
        //Areas occupied by the objects drawn on the main screen are masked out as 0
        opStencil.run();

        //Add content of the main screen to the stencil
        opAdd.run();

        //Render to the final screen and present it
        finalScreen.draw();
        window.present(finalScreen);

        //Update animation
        animUpdater.update();
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

        opStencil = finalScreen.createTextureOperation(
                Arrays.asList(skyboxColorTexture, mainStencilTexture),
                new ArrayList<>(),
                true
        );

        var texOpStencilParams = new TextureOperationParameters();
        texOpStencilParams.setOperationType(TextureOperationParameters.OperationType.MUL);
        opStencil.setParameters(texOpStencilParams);

        //Add rendering result of main screen to the stencil
        MttTexture stencilTexture = opStencil.getResultTexture();
        MttTexture mainColorTexture = mainScreen.texturize(ScreenImageType.COLOR, finalScreen);

        opAdd = finalScreen.createTextureOperation(
                Arrays.asList(stencilTexture, mainColorTexture),
                new ArrayList<>(),
                true
        );

        var texOpAddParams = new TextureOperationParameters();
        texOpAddParams.setOperationType(TextureOperationParameters.OperationType.ADD);
        opAdd.setParameters(texOpAddParams);

        //Set result texture of add operation as final output
        texturedQuad.replaceTexture(opAdd.getResultTexture());
    }
}
