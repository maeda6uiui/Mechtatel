package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.texture.TextureOperationParameters;

import java.io.IOException;
import java.util.ArrayList;

public class TextureOperationTest extends Mechtatel {
    public TextureOperationTest(MttSettings settings) {
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

        new TextureOperationTest(settings);
    }

    private MttScreen firstScreen;
    private MttScreen secondScreen;
    private MttScreen finalScreen;

    private MttTexture firstColorTexture;
    private MttTexture firstDepthTexture;
    private MttTexture secondColorTexture;
    private MttTexture secondDepthTexture;
    private MttTexture finalTexture;

    private Model3D firstModel;
    private Model3D secondModel;

    private FreeCamera camera;

    @Override
    public void init() {
        firstScreen = this.createScreen(
                "first",
                2048,
                2048,
                -1,
                -1,
                true,
                null
        );
        secondScreen = this.createScreen(
                "second",
                2048,
                2048,
                -1,
                -1,
                true,
                null
        );
        finalScreen = this.createScreen(
                "final",
                2048,
                2048,
                -1,
                -1,
                true,
                null
        );

        var screenDrawOrder = new ArrayList<String>();
        screenDrawOrder.add("first");
        screenDrawOrder.add("second");
        this.setScreenDrawOrder(screenDrawOrder);

        var deferredScreenDrawOrder = new ArrayList<String>();
        deferredScreenDrawOrder.add("final");
        this.setDeferredScreenDrawOrder(deferredScreenDrawOrder);

        this.setPresentScreenName("final");

        var textureOperationOrder = new ArrayList<String>();
        textureOperationOrder.add("test");
        this.setTextureOperationOrder(textureOperationOrder);

        try {
            firstModel = this.createModel3D("first", "./Mechtatel/Model/Cube/cube.obj");
            secondModel = this.createModel3D("second", "./Mechtatel/Model/Teapot/teapot.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera = new FreeCamera(firstScreen.getCamera());
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

        if (firstColorTexture != null) {
            firstColorTexture.cleanup();
            firstDepthTexture.cleanup();
            secondColorTexture.cleanup();
            secondDepthTexture.cleanup();
            finalTexture.cleanup();
        }

        firstColorTexture = this.texturizeColorOfScreen("first", "final");
        firstDepthTexture = this.texturizeDepthOfScreen("first", "final");
        secondColorTexture = this.texturizeColorOfScreen("second", "final");
        secondDepthTexture = this.texturizeDepthOfScreen("second", "final");

        var textureOperationParameters = new TextureOperationParameters();
        textureOperationParameters.setOperationType(TextureOperationParameters.TEXTURE_OPERATION_ADD);

        finalTexture = this.createTextureOperation(
                "test",
                firstColorTexture,
                secondColorTexture,
                firstDepthTexture,
                secondDepthTexture,
                "final",
                textureOperationParameters
        );
    }
}
