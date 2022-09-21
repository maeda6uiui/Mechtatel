package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import org.joml.Vector2f;

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
        var axes = this.createAxesLine3DSet(10.0f);
        var texturedQuad = this.createTexturedQuad2D(
                "./Mechtatel/Texture/lenna.jpg",
                new Vector2f(-0.5f, -0.5f),
                new Vector2f(0.5f, 0.5f),
                0.0f);

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
