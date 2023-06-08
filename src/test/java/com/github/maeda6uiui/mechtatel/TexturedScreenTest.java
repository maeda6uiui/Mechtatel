package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.DrawPath;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.ScreenCreator;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel3D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;

public class TexturedScreenTest extends Mechtatel {
    public TexturedScreenTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = MttSettings.load("./Mechtatel/Setting/settings.json");
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new TexturedScreenTest(settings);
    }

    private MttScreen primaryScreen;
    private MttModel3D primaryCube;
    private FreeCamera camera;

    private MttScreen secondaryScreen;
    private MttModel3D secondaryCube;
    private Vector3f secondaryCameraPosition;

    @Override
    public void init() {
        var primaryScreenCreator = new ScreenCreator(this, "primary");
        primaryScreen = primaryScreenCreator.create();
        primaryScreen.setBackgroundColor(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        primaryScreen.getCamera().setEye(new Vector3f(2.0f, 2.0f, 2.0f));

        camera = new FreeCamera(primaryScreen.getCamera());

        var secondaryScreenCreator = new ScreenCreator(this, "secondary");
        secondaryScreenCreator.addPostProcessingNabor("parallel_light");
        secondaryScreenCreator.setDepthImageSize(1024, 1024);
        secondaryScreenCreator.setScreenSize(512, 512);
        secondaryScreenCreator.setShouldChangeExtentOnRecreate(false);
        secondaryScreen = secondaryScreenCreator.create();
        secondaryScreen.setBackgroundColor(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        secondaryScreen.createParallelLight();

        secondaryCameraPosition = new Vector3f(1.2f, 1.2f, 1.2f);
        secondaryScreen.getCamera().setEye(secondaryCameraPosition);

        var drawPath = new DrawPath(this);
        drawPath.addToScreenDrawOrder("secondary");
        drawPath.addToScreenDrawOrder("primary");
        drawPath.setPresentScreenName("primary");
        drawPath.apply();

        try {
            primaryCube = this.createModel3D("primary", "./Mechtatel/Standard/Model/Cube/cube.obj");
            secondaryCube = this.createModel3D("secondary", "./Mechtatel/Standard/Model/Cube/cube.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        secondaryCameraPosition = new Matrix4f().rotateY(0.01f).transformPosition(secondaryCameraPosition);
        secondaryScreen.getCamera().setEye(secondaryCameraPosition);

        MttTexture secondaryDrawResult = this.texturizeColorOfScreen("secondary", "primary");
        primaryCube.replaceTexture(0, secondaryDrawResult);
    }
}
