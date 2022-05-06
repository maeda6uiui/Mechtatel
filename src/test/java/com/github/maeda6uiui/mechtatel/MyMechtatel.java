package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import org.joml.Vector3f;
import org.joml.Vector4f;

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

    @Override
    public void init() {
        var lineSet = this.createLine3DSet();
        for (int x = -100; x <= 100; x++) {
            lineSet.add(
                    new Vector3f((float) x, 0.0f, -100.0f),
                    new Vector3f((float) x, 0.0f, 100.0f),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
            );
        }
        for (int z = -100; z <= 100; z++) {
            lineSet.add(
                    new Vector3f(-100.0f, 0.0f, (float) z),
                    new Vector3f(100.0f, 0.0f, (float) z),
                    new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
            );
        }
        lineSet.createBuffer();

        var camera = this.getCamera();
        camera.setEye(new Vector3f(50.0f, 20.0f, 50.0f));

        var ppNaborNames = new ArrayList<String>();
        ppNaborNames.add("fog");
        this.createPostProcessingNabors(ppNaborNames);

        var fog = this.getFog();
        fog.setStart(50.0f);
        fog.setEnd(100.0f);
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
