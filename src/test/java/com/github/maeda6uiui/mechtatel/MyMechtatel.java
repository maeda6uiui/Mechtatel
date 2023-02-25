package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttListbox;

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
        var items = new ArrayList<String>();
        for (int i = 0; i < 30; i++) {
            items.add(String.format("Item%d", i));
        }

        MttListbox listbox = this.createMttListbox(
                -0.9f, -0.9f, 0.5f, 1.8f,
                Font.SERIF, Font.PLAIN, 60, Color.WHITE, Color.WHITE, items, 0.07f);
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
