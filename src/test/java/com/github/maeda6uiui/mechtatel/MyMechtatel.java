package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttButtonSettings;
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

    @Override
    public void init() {
        for (int i = 0; i < 10; i++) {
            var buttonSettings = new MttButtonSettings();
            buttonSettings.topLeft = new Vector2f(-0.9f, -0.9f + i * 0.1f);
            buttonSettings.bottomRight = new Vector2f(-0.55f, -0.9f + (i + 1) * 0.1f);
            var button = this.createMttButton(buttonSettings);
        }
        for (int i = 0; i < 10; i++) {
            var buttonSettings = new MttButtonSettings();
            buttonSettings.topLeft = new Vector2f(-0.5f, -0.9f + i * 0.1f);
            buttonSettings.bottomRight = new Vector2f(-0.15f, -0.9f + (i + 1) * 0.1f);
            var button = this.createMttButton(buttonSettings);
        }
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
