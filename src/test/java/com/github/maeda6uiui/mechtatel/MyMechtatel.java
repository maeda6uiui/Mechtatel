package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.Sphere3D;
import com.github.maeda6uiui.mechtatel.core.sound.Sound3D;
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

    private Sound3D sound;
    private Vector3f soundSourcePosition;
    private Sphere3D sphere;

    @Override
    public void init() {
        var axes = this.createPositiveAxesLine3DSet(10.0f);
        camera = new FreeCamera(this.getCamera());

        soundSourcePosition = new Vector3f(5.0f, 0.0f, 0.0f);

        sound = this.createSound3D("./Mechtatel/Sound/sample_monaural.ogg", false, false);
        sound.setPosition(soundSourcePosition);
        sound.play();

        sphere = this.createSphere3D(soundSourcePosition, 1.0f, 16, 16, new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
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

        soundSourcePosition = soundSourcePosition.rotateY(0.01f);
        sphere.rotY(0.01f);
        sound.setPosition(soundSourcePosition);
    }
}
