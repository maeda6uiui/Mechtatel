package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.ArrayList;

public class TexturedScreenTest extends Mechtatel {
    public TexturedScreenTest(MttSettings settings) {
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

        new TexturedScreenTest(settings);
    }

    private MttScreen primaryScreen;
    private Model3D primaryCube;
    private FreeCamera camera;

    private MttScreen secondaryScreen;
    private Model3D secondaryCube;

    private Vector3f secondaryCameraPosition;

    @Override
    public void init() {
        //Set up primary screen
        primaryScreen = this.createScreen(
                "primary",
                2048,
                2048,
                -1,
                -1,
                true,
                null
        );
        primaryScreen.setBackgroundColor(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        primaryScreen.getCamera().setEye(new Vector3f(2.0f, 2.0f, 2.0f));

        camera = new FreeCamera(primaryScreen.getCamera());

        //Set up secondary screen
        var ppNaborNames = new ArrayList<String>();
        ppNaborNames.add("parallel_light");
        secondaryScreen = this.createScreen(
                "secondary",
                1024,
                1024,
                512,
                512,
                false,
                ppNaborNames
        );
        secondaryScreen.setBackgroundColor(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));
        secondaryScreen.createParallelLight();

        secondaryCameraPosition = new Vector3f(1.2f, 1.2f, 1.2f);
        secondaryScreen.getCamera().setEye(secondaryCameraPosition);

        var screenDrawOrder = new ArrayList<String>();
        screenDrawOrder.add("secondary");
        screenDrawOrder.add("primary");
        this.setScreenDrawOrder(screenDrawOrder);

        this.setPresentScreenName("primary");

        try {
            primaryCube = this.createModel3D("primary", "./Mechtatel/Model/Cube/cube.obj");
            secondaryCube = this.createModel3D("secondary", "./Mechtatel/Model/Cube/cube.obj");
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
