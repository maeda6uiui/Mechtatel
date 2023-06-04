package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttTextarea;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter.JISKeyInterpreter;

import java.awt.*;
import java.io.IOException;

public class TextareaTest extends Mechtatel {
    public TextareaTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = new MttSettings("./Mechtatel/Setting/settings.json");
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new TextareaTest(settings);
    }

    private MttTextarea textarea;

    @Override
    public void init() {
        var keyInterpreter = new JISKeyInterpreter();
        textarea = this.createTextarea(
                -0.9f, -0.9f, 0.9f, 0.9f, 0.1f, 0.001f, 0.01f,
                Font.SANS_SERIF, Font.PLAIN, 32, Color.GREEN, Color.WHITE, Color.LIGHT_GRAY,
                0.5f, 0.5f, keyInterpreter, MttTextarea.DEFAULT_SUPPORTED_CHARACTERS);
    }

    @Override
    public void update() {
        if (this.getKeyboardPressingCount("LEFT_SHIFT") > 0
                && this.getKeyboardPressingCount("ENTER") == 1) {
            System.out.println(textarea.getText());
        }
    }
}
