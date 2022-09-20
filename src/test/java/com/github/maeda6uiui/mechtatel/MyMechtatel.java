package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.Vertex3DUV;
import org.joml.Vector2f;
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

    private FreeCamera camera;

    @Override
    public void init() {
        var v1 = new Vertex3DUV(new Vector3f(-5.0f, 0.0f, -5.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 0.0f));
        var v2 = new Vertex3DUV(new Vector3f(-5.0f, 0.0f, 5.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(0.0f, 1.0f));
        var v3 = new Vertex3DUV(new Vector3f(5.0f, 0.0f, 5.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 1.0f));
        var v4 = new Vertex3DUV(new Vector3f(5.0f, 0.0f, -5.0f), new Vector4f(1.0f, 1.0f, 1.0f, 1.0f), new Vector2f(1.0f, 0.0f));

        var vertices = new ArrayList<Vertex3DUV>();
        vertices.add(v1);
        vertices.add(v2);
        vertices.add(v3);
        vertices.add(v4);

        var axes = this.createAxesLine3DSet(10.0f);

        var texturedQuad = this.createTexturedQuad3D("./Mechtatel/Texture/lenna.jpg", true, vertices);

        var model = this.createModel3D("./Mechtatel/Model/Plane/plane.obj");

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
