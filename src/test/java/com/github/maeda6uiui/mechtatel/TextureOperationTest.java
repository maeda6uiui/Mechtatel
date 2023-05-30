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
import org.joml.Vector3f;

import java.io.IOException;

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

    private MttTexturedQuad2D texturedQuad;

    private MttModel3D firstModel;
    private MttModel3D secondModel;

    private FreeCamera camera;

    @Override
    public void init() {
        var firstScreenCreator = new ScreenCreator(this, "first");
        firstScreen = firstScreenCreator.create();
        firstScreen.getCamera().setEye(new Vector3f(2.0f, 2.0f, 2.0f));

        var secondScreenCreator = new ScreenCreator(this, "second");
        secondScreen = secondScreenCreator.create();
        secondScreen.getCamera().setEye(new Vector3f(1.0f, 2.0f, 1.0f));

        var finalScreenCreator = new ScreenCreator(this, "final");
        finalScreen = finalScreenCreator.create();

        var drawPath = new DrawPath(this);
        drawPath.addToScreenDrawOrder("first");
        drawPath.addToScreenDrawOrder("second");
        drawPath.addToTextureOperationOrder("test");
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
            firstModel = this.createModel3D("first", "./Mechtatel/Model/Plane/plane.obj");
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

        //You need to create textures from screens in every frame if you enable resizing of the window.
        //This is because resizing of the window causes many resources of the screens to be recreated,
        //and previously created images that belong to screens are no longer valid.
        //
        //You cannot use reshape() to this end because reshape() is invoked from a higher layer
        //and is not in sync with Vulkan's procedure of rendering. (maybe...)
        //
        //You should run cleanup() of textures created here after you are done with them,
        //otherwise unused texture allocations are accumulated.
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
        texturedQuad.replaceTexture(finalTexture);
    }

    @Override
    public void postPresent() {
        firstColorTexture.cleanup();
        firstDepthTexture.cleanup();
        secondColorTexture.cleanup();
        secondDepthTexture.cleanup();
        finalTexture.cleanup();
    }
}
