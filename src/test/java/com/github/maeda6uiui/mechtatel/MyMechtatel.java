package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttFont;
import com.github.maeda6uiui.mechtatel.core.text.TextUtil;
import org.joml.Vector2f;

import java.awt.*;
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

    private MttFont mttFont;

    @Override
    public void init() {
        var axes = this.createAxesLine3DSet(10.0f);

        camera = new FreeCamera(this.getCamera());

        var texts = new ArrayList<String>();
        texts.add("Hello!");
        texts.add("こんにちは！");
        texts.add("Здравствуйте!");
        texts.add("你好！");
        String requiredChars = TextUtil.getRequiredChars(texts);

        mttFont = this.createMttFont(
                new Font("Sans", Font.PLAIN, 50),
                false,
                Color.WHITE,
                requiredChars);
        mttFont.prepare(
                texts,
                new Vector2f(-1.0f, -1.0f),
                0.0f,
                0.002f,
                0.004f,
                0.0f);
        mttFont.createBuffers();
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
