package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttTextarea;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter.JISKeyInterpreter;

import java.awt.*;

public class TextareaTest extends Mechtatel {
    public TextareaTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        TextareaTest::new,
                        () -> {
                            System.out.println("Failed to load settings");
                        }
                );
    }

    private MttTextarea textarea;

    @Override
    public void init() {
        var keyInterpreter = new JISKeyInterpreter();
        textarea = this.createTextarea(
                new MttTextarea.MttTextareaCreateInfo()
                        .setX(-0.9f)
                        .setY(-0.9f)
                        .setWidth(0.9f)
                        .setHeight(0.9f)
                        .setCaretLength(0.1f)
                        .setCaretMarginX(0.001f)
                        .setCaretMarginY(0.01f)
                        .setFontName(Font.SANS_SERIF)
                        .setFontStyle(Font.PLAIN)
                        .setFontSize(32)
                        .setFontColor(Color.GREEN)
                        .setFrameColor(Color.WHITE)
                        .setCaretColor(Color.LIGHT_GRAY)
                        .setCaretBlinkInterval(0.5f)
                        .setSecondsPerFrame(this.getSecondsPerFrame())
                        .setRepeatDelay(0.5f)
                        .setKeyInterpreter(keyInterpreter)
                        .setSupportedCharacters(MttTextarea.DEFAULT_SUPPORTED_CHARACTERS)
        );
    }

    @Override
    public void update() {
        if (this.getKeyboardPressingCount("F1") == 1) {
            System.out.println(textarea.getText());
        } else if (this.getKeyboardPressingCount("F2") == 1) {
            System.out.println(textarea.getLines());
        } else if (this.getKeyboardPressingCount("F3") == 1) {
            textarea.clear();
        }
    }
}