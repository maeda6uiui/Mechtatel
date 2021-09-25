package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import java.io.IOException;

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

    private Model3D model;

    private Vector3f lightPosition;
    private Vector3f lightCenter;

    private Vector3f cameraPosition;
    private Vector3f cameraCenter;

    @Override
    public void init() {
        model = this.createModel3D("./Mechtatel/Model/Teapot/teapot.obj");

        lightPosition = new Vector3f(100.0f, 100.0f, 100.0f);
        lightCenter = new Vector3f(0.0f, 0.0f, 0.0f);
        var lightDirection = lightCenter.sub(lightPosition).normalize();
        this.getParallelLight().setDirection(lightDirection);

        cameraPosition = new Vector3f(1.0f, 1.5f, 1.0f);
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
        new Matrix4f().rotateY((float) Math.toRadians(1.0)).transformPosition(lightPosition);
        var lightDirection = lightCenter.sub(lightPosition).normalize();
        this.getParallelLight().setDirection(lightDirection);
         */

        new Matrix4f().rotateY((float) Math.toRadians(1.0)).transformPosition(cameraPosition);
        this.getCamera().setEye(cameraPosition);
    }
}
