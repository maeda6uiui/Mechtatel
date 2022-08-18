package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import org.joml.Vector3f;
import org.joml.Vector4f;

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

    @Override
    public void init() {
        var capsule = this.createCapsule3D(
                new Vector3f(0.0f, 1.0f, 0.0f),
                new Vector3f(0.0f, -1.0f, 0.0f),
                1.0f,
                32,
                32,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
        );
        var axes = this.createPositiveAxesLine3DSet();
    }

    @Override
    public void dispose() {

    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {

    }
}
