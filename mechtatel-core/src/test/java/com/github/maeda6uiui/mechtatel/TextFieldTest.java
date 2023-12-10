package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter.JISKeyInterpreter;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.gui.MttTextField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class TextFieldTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(TextFieldTest.class);

    public TextFieldTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        TextFieldTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttTextField textField;

    @Override
    public void init(MttWindow window) {
        var keyInterpreter = new JISKeyInterpreter();

        MttScreen defaultScreen = window.getDefaultScreen();
        textField = defaultScreen.createTextField(
                new MttTextField.MttTextFieldCreateInfo()
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
                        .setSupportedCharacters(MttTextField.DEFAULT_SUPPORTED_CHARACTERS)
        );
    }

    @Override
    public void update(MttWindow window) {
        if (window.getKeyboardPressingCount(KeyCode.F1) == 1) {
            logger.info(textField.getText());
        } else if (window.getKeyboardPressingCount(KeyCode.F2) == 1) {
            textField.clear();
        }

        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
