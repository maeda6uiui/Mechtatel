package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.component.Sphere3D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.ArrayList;

public class ScreenshotTest extends Mechtatel {
    public ScreenshotTest(MttSettings settings) {
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

        new ScreenshotTest(settings);
    }

    private FreeCamera camera;

    private Model3D plane;
    private Model3D cube;
    private Sphere3D sphere;

    @Override
    public void init() {
        var ppNaborNames = new ArrayList<String>();
        ppNaborNames.add("parallel_light");
        ppNaborNames.add("fog");
        ppNaborNames.add("shadow_mapping");
        MttScreen mainScreen=this.createScreen(
                "main",
                2048,
                2048,
                -1,
                -1,
                true,
                ppNaborNames
        );
        mainScreen.setShouldPresent(true);

        var screenDrawOrder = new ArrayList<String>();
        screenDrawOrder.add("main");
        this.setScreenDrawOrder(screenDrawOrder);

        camera=new FreeCamera(mainScreen.getCamera());

        try {
            plane = this.createModel3D("main", "./Mechtatel/Model/Plane/plane.obj");
            cube = this.createModel3D("main", "./Mechtatel/Model/Cube/cube.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        cube.translate(new Vector3f(0.0f, 1.0f, 0.0f));

        sphere = this.createSphere3D(
                new Vector3f(5.0f, 2.0f, 0.0f),
                2.0f, 32, 32, new Vector4f(1.0f, 0.0f, 1.0f, 1.0f)
        );

        mainScreen.createParallelLight();

        mainScreen.getFog().setStart(10.0f);
        mainScreen.getFog().setEnd(20.0f);
    }

    @Override
    public void dispose() {

    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {
        if (this.getKeyboardPressingCount("ENTER") == 1) {
            try {
                this.saveScreenshot("main", "bgra", "screenshot.png");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

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
    }
}
