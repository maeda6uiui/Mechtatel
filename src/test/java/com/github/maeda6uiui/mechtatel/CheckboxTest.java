package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;

import java.awt.*;
import java.io.IOException;

public class CheckboxTest extends Mechtatel {
    public CheckboxTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = MttSettings.load("./Mechtatel/Setting/settings.json");
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new CheckboxTest(settings);
    }

    @Override
    public void init() {
        this.createCheckbox(
                -0.9f, -0.9f, 0.9f, 0.1f,
                -0.9f, -0.9f, 0.05f, 0.1f, -0.8f, -0.93f,
                "This is a sample checkbox", Font.SANS_SERIF, Font.PLAIN, 64,
                Color.GREEN, Color.WHITE
        );
        this.createCheckbox(
                -0.9f, -0.75f, 0.9f, 0.1f,
                -0.9f, -0.75f, 0.05f, 0.1f, -0.8f, -0.78f,
                "これはチェックボックスのサンプルです", Font.SANS_SERIF, Font.PLAIN, 64,
                Color.ORANGE, Color.GREEN
        );
    }
}
