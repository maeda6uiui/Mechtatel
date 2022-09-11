package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.Vertex2D;
import org.joml.Vector2f;
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

    private FreeCamera camera;

    @Override
    public void init() {
        var capsule = this.createCapsule3D(
                new Vector3f(0.0f, 0.0f, 0.0f),
                2.0f,
                2.0f,
                32,
                32,
                new Vector4f(1.0f, 1.0f, 1.0f, 1.0f)
        );
        var axes = this.createPositiveAxesLine3DSet(10.0f);
        var line = this.createLine2D(
                new Vertex2D(new Vector2f(-1.0f, -1.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)),
                new Vertex2D(new Vector2f(1.0f, 1.0f), new Vector4f(1.0f, 0.0f, 0.0f, 1.0f)),
                0.0f
        );

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
