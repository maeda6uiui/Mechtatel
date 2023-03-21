package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;

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

    @Override
    public void init() {
        var itemTexts = new ArrayList<String>();
        for (int i = 0; i < 30; i++) {
            itemTexts.add(String.format("Item %d", i));
        }
        this.createMttListbox(
                -0.9f, -0.9f, 0.9f, 0.91f,
                Font.SANS_SERIF, Font.PLAIN, 32, Color.LIGHT_GRAY, Color.WHITE,
                itemTexts, 0.1f, Font.SANS_SERIF, Font.BOLD, 32, Color.WHITE);
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
