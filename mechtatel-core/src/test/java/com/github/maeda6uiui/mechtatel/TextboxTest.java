package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttTextbox;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter.JISKeyInterpreter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class TextboxTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(TextboxTest.class);

    public TextboxTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        TextboxTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttTextbox textbox;

    @Override
    public void init(MttWindow window) {
        var keyInterpreter = new JISKeyInterpreter();
        textbox = window.createTextbox(
                new MttTextbox.MttTextboxCreateInfo()
                        .setX(-0.9f)
                        .setY(-0.9f)
                        .setWidth(0.9f)
                        .setHeight(0.1f)
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
                        .setSupportedCharacters(MttTextbox.DEFAULT_SUPPORTED_CHARACTERS)
        );
    }

    @Override
    public void update(MttWindow window) {
        if (window.getKeyboardPressingCount("F1") == 1) {
            System.out.println(textbox.getText());
        } else if (window.getKeyboardPressingCount("F2") == 1) {
            textbox.clear();
        }
    }
}
