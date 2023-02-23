package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttButton;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttGuiComponentCallbacks;

import java.awt.*;
import java.io.IOException;

class MyButtonCallbacks extends MttGuiComponentCallbacks {
    @Override
    public void onLButtonDown() {
        System.out.println("Left Button Down");
    }
}

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
        MttButton button = this.createMttButton(
                -0.9f, -0.9f, 0.18f, 0.1f,
                "ボタンです", Font.SERIF, Font.PLAIN, 65, Color.WHITE, Color.WHITE);
        button.setCallbacks(new MyButtonCallbacks());
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
