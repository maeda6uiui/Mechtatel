package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import org.joml.Vector2f;
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

    private FreeCamera camera;

    @Override
    public void init() {
        var lineSet = this.createLine2DSet();
        lineSet.add(new Vector2f(-0.5f, -0.5f), new Vector2f(0.5f, -0.5f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), 0.0f);
        lineSet.add(new Vector2f(-0.5f, 0.0f), new Vector2f(0.5f, 0.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), 0.0f);
        lineSet.add(new Vector2f(-0.5f, 0.5f), new Vector2f(0.5f, 0.5f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f), 0.0f);
        lineSet.createBuffer();

        camera = new FreeCamera(this.getCamera());
    }

    @Override
    public void dispose() {

    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {
        int keyFront = this.getKeyboardPressingCount("W");
        int keyBack = this.getKeyboardPressingCount("S");
        int keyLeft = this.getKeyboardPressingCount("A");
        int keyRight = this.getKeyboardPressingCount("D");
        camera.translate(keyFront, keyBack, keyLeft, keyRight);

        int keyRotateTop = this.getKeyboardPressingCount("UP");
        int keyRotateBottom = this.getKeyboardPressingCount("DOWN");
        int keyRotateLeft = this.getKeyboardPressingCount("LEFT");
        int keyRotateRight = this.getKeyboardPressingCount("RIGHT");
        camera.rotate(keyRotateTop, keyRotateBottom, keyRotateLeft, keyRotateRight);
    }
}
