package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttTextbox;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter.JISKeyInterpreter;

import java.awt.*;
import java.io.IOException;

public class TextboxTest extends Mechtatel {
    public TextboxTest(MttSettings settings) {
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

        new TextboxTest(settings);
    }

    private MttTextbox textbox;

    @Override
    public void init() {
        var keyInterpreter = new JISKeyInterpreter();
        textbox = this.createTextbox(
                -0.9f, -0.9f, 0.9f, 0.1f,
                0.001f, 0.01f, Font.SANS_SERIF, Font.PLAIN, 32,
                Color.GREEN, Color.WHITE, Color.LIGHT_GRAY, 0.5f, 0.5f,
                keyInterpreter, MttTextbox.DEFAULT_SUPPORTED_CHARACTERS);
    }

    @Override
    public void update() {
        if (this.getKeyboardPressingCount("F1") == 1) {
            System.out.println(textbox.getText());
        } else if (this.getKeyboardPressingCount("F2") == 1) {
            textbox.clear();
        }
    }
}
