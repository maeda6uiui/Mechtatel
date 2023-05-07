package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.component.Sphere3D;
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
        //Load settings from a JSON file
        try {
            settings = new MttSettings("./Mechtatel/Setting/settings.json");
        }
        //If the program fails to load the JSON file, then use the default settings
        catch (IOException e) {
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
        camera = new FreeCamera(this.getCamera());

        try {
            plane = this.createModel3D("./Mechtatel/Model/Plane/plane.obj");
            cube = this.createModel3D("./Mechtatel/Model/Cube/cube.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        cube.translate(new Vector3f(0.0f, 1.0f, 0.0f));

        sphere = this.createSphere3D(
                new Vector3f(5.0f, 2.0f, 0.0f),
                2.0f, 32, 32, new Vector4f(1.0f, 0.0f, 1.0f, 1.0f)
        );

        var naborNames = new ArrayList<String>();
        naborNames.add("parallel_light");
        naborNames.add("fog");
        naborNames.add("shadow_mapping");
        this.createPostProcessingNabors(naborNames);

        this.createParallelLight();

        this.getFog().setStart(10.0f);
        this.getFog().setEnd(20.0f);
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
                this.saveScreenshot("bgra", "screenshot.png");
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
