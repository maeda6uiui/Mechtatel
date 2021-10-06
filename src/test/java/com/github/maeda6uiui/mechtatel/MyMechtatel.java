package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.light.PointLight;
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

    private Vector3f cameraPosition;
    private Vector3f cameraCenter;

    @Override
    public void init() {
        ground = this.createModel3D("./Mechtatel/Model/Plane/plane.obj");

        this.setPointLightAmbientColor(new Vector3f(0.0f, 0.0f, 0.0f));

        PointLight pointLightR = this.createPointLight();
        pointLightR.setPosition(new Vector3f(-5.0f, 5.0f, -5.0f));
        pointLightR.setDiffuseColor(new Vector3f(1.0f, 0.0f, 0.0f));

        PointLight pointLightG = this.createPointLight();
        pointLightG.setPosition(new Vector3f(0.0f, 5.0f, 0.0f));
        pointLightG.setDiffuseColor(new Vector3f(0.0f, 1.0f, 0.0f));

        PointLight pointLightB = this.createPointLight();
        pointLightB.setPosition(new Vector3f(5.0f, 5.0f, 5.0f));
        pointLightB.setDiffuseColor(new Vector3f(0.0f, 0.0f, 1.0f));

        var ppNaborNames = new ArrayList<String>();
        ppNaborNames.add("point_light");
        this.createPostProcessingNabors(ppNaborNames);

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
