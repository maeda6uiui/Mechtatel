package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import org.joml.Vector3f;

import java.io.IOException;
import java.util.ArrayList;

public class MyMechtatel extends Mechtatel {
    public MyMechtatel(MttSettings settings) {
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
            settings = new MttSettings();
        }

        new MyMechtatel(settings);
    }

    private Model3D ground;
    private Model3D[] cubes;

    private Vector3f cameraPosition;
    private Vector3f cameraCenter;

    @Override
    public void init() {
        ground = this.createModel3D("./Mechtatel/Model/Plane/plane.obj");

        cubes = new Model3D[5];
        cubes[0] = this.createModel3D("./Mechtatel/Model/Cube/cube.obj");
        for (int i = 1; i < 5; i++) {
            cubes[i] = this.duplicateModel3D(cubes[0]);
        }
        cubes[0].translate(new Vector3f(-3.0f, 1.0f, -3.0f));
        cubes[1].translate(new Vector3f(-3.0f, 1.0f, 3.0f));
        cubes[2].translate(new Vector3f(3.0f, 1.0f, -3.0f));
        cubes[3].translate(new Vector3f(3.0f, 1.0f, 3.0f));
        cubes[4].translate(new Vector3f(0.0f, 1.0f, 0.0f));
        cubes[4].rescale(new Vector3f(1.0f, 2.0f, 1.0f));

        var ppNaborNames = new ArrayList<String>();
        ppNaborNames.add("parallel_light");
        this.createPostProcessingNabors(ppNaborNames);

        cameraPosition = new Vector3f(5.0f, 5.0f, 5.0f);
        cameraCenter = new Vector3f(0.0f, 0.0f, 0.0f);
        this.getCamera().setEye(cameraPosition);
        this.getCamera().setCenter(cameraCenter);
    }

    @Override
    public void dispose() {
        //Components are automatically cleaned up, so you don't have to explicitly clean up the components.
        //model.cleanup();
    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {
        int pressingCount = this.getKeyboardPressingCount("A");
        int releasingCount = this.getKeyboardReleasingCount("A");
        System.out.printf("Pressing: %d  Releasing: %d\n", pressingCount, releasingCount);
    }
}
