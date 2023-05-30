package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.DrawPath;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.ScreenCreator;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel3D;
import com.github.maeda6uiui.mechtatel.core.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.texture.TextureOperationParameters;
import org.joml.Vector2f;

import java.io.IOException;

public class SkyboxTest extends Mechtatel {
    public SkyboxTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = new MttSettings("./Mechtatel/Setting/settings.json");
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new SkyboxTest(settings);
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

    private MttModel3D skyboxModel;
    private MttModel3D mainModel;

    private FreeCamera camera;

    @Override
    public void init() {
        var skyboxScreenCreator = new ScreenCreator(this, "skybox");
        skyboxScreenCreator.setDepthImageSize(1024, 1024);
        skyboxScreen = skyboxScreenCreator.create();
        skyboxScreen.getCamera().setZNear(200.0f);
        skyboxScreen.getCamera().setZFar(2000.0f);

        var mainScreenCreator = new ScreenCreator(this, "main");
        mainScreen = mainScreenCreator.create();

        var finalScreenCreator = new ScreenCreator(this, "final");
        finalScreen = finalScreenCreator.create();

        var drawPath = new DrawPath(this);
        drawPath.addToScreenDrawOrder("skybox");
        drawPath.addToScreenDrawOrder("main");
        drawPath.addToTextureOperationOrder("merge_by_depth");
        drawPath.addToDeferredScreenDrawOrder("final");
        drawPath.setPresentScreenName("final");
        drawPath.apply();

        var dummyTexture = this.createTexture("final", "./Mechtatel/Texture/lenna.jpg", false);
        texturedQuad = this.createTexturedQuad2D(
                "final",
                dummyTexture,
                new Vector2f(-1.0f, -1.0f),
                new Vector2f(1.0f, 1.0f),
                0.0f
        );

        try {
            skyboxModel = this.createModel3D("skybox", "./Mechtatel/Model/Skybox/skybox.obj");
            mainModel = this.createModel3D("main", "./Mechtatel/Model/Cube/cube.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera = new FreeCamera(mainScreen.getCamera());
    }

    @Override
    public void update() {
        camera.translate(
                this.getKeyboardPressingCount("W"),
                this.getKeyboardPressingCount("S"),
                this.getKeyboardPressingCount("A"),
                this.getKeyboardPressingCount("D")
        );
        camera.rotate(
                this.getKeyboardPressingCount("UP"),
                this.getKeyboardPressingCount("DOWN"),
                this.getKeyboardPressingCount("LEFT"),
                this.getKeyboardPressingCount("RIGHT")
        );
        skyboxScreen.syncCamera(mainScreen.getCamera());

        skyboxColorTexture = this.texturizeColorOfScreen("skybox", "final");
        skyboxDepthTexture = this.texturizeDepthOfScreen("skybox", "final");
        mainColorTexture = this.texturizeColorOfScreen("main", "final");
        mainDepthTexture = this.texturizeDepthOfScreen("main", "final");

        var textureOperationParameters = new TextureOperationParameters();
        textureOperationParameters.setOperationType(TextureOperationParameters.TEXTURE_OPERATION_MERGE_BY_DEPTH);
        textureOperationParameters.setFirstTextureFixedDepth(0.99f);

        finalTexture = this.createTextureOperation(
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
    public void postPresent() {
        if (this.getKeyboardPressingCount("ENTER") == 1) {
            try {
                this.saveScreenshot("final", "bgra", "screenshot.jpg");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        skyboxColorTexture.cleanup();
        skyboxDepthTexture.cleanup();
        mainColorTexture.cleanup();
        mainDepthTexture.cleanup();
        finalTexture.cleanup();
    }
}
