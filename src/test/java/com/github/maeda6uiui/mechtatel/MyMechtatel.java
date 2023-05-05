package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttLabel;

import java.awt.*;
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
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new MyMechtatel(settings);
    }

    private int enterPressCount;
    private MttLabel label;

    @Override
    public void init() {
        enterPressCount = 0;

        var requiredChars = "0123456789";
        label = this.createMttLabel(
                -0.9f, -0.9f, 0.9f, 0.2f, requiredChars,
                Font.SANS_SERIF, Font.PLAIN, 32, Color.WHITE, Color.WHITE);
        label.prepare(String.valueOf(enterPressCount));
    }

    @Override
    public void dispose() {

    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {
        if (this.getKeyboardPressingCount("ENTER") == 1) {
            enterPressCount++;
            label.prepare(String.valueOf(enterPressCount));
        }
    }
}
