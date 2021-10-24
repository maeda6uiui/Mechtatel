package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import org.joml.Matrix4f;
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
    private Model3D model1;
    private Model3D model2;

    private Vector3f cameraPosition;
    private Vector3f cameraCenter;

    @Override
    public void init() {
        ground = this.createModel3D("./Mechtatel/Model/Plane/plane.obj");

        model1 = this.createModel3D("./Mechtatel/Model/Cube/cube.obj");
        model2 = this.duplicateModel3D(model1);

        model1.translate(new Vector3f(-3.0f, 1.0f, -3.0f));
        model2.translate(new Vector3f(3.0f, 1.0f, 3.0f));

        var ppNaborNames = new ArrayList<String>();
        ppNaborNames.add("parallel_light");
        ppNaborNames.add("shadow_mapping");
        this.createPostProcessingNabors(ppNaborNames);

        ParallelLight parallelLight = this.createParallelLight();

        cameraPosition = new Vector3f(4.0f, 4.0f, 4.0f);
        cameraCenter = new Vector3f(0.0f, 0.5f, 0.0f);
        this.getCamera().setEye(cameraPosition);
        this.getCamera().setCenter(cameraCenter);
    }

    @Override
    public void dispose() {
        //Components are automatically cleaned up, so you don't have to explicitly clean up the component.
        //model.cleanup();
    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {
        /*
        new Matrix4f().rotateY((float) Math.toRadians(0.3)).transformPosition(lightPosition);
        var lightDirection = lightCenter.sub(lightPosition).normalize();
        this.getParallelLight().setDirection(lightDirection);
         */

        new Matrix4f().rotateY((float) Math.toRadians(0.3)).transformPosition(cameraPosition);
        this.getCamera().setEye(cameraPosition);
    }
}
