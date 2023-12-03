package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.gui.MttCheckBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class CheckboxTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(CheckboxTest.class);

    public CheckboxTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        CheckboxTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    @Override
    public void init(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.createCheckBox(
                new MttCheckBox.MttCheckBoxCreateInfo()
                        .setX(-0.9f)
                        .setY(-0.9f)
                        .setWidth(0.9f)
                        .setHeight(0.1f)
                        .setBoxX(-0.9f)
                        .setBoxY(-0.9f)
                        .setBoxWidth(0.05f)
                        .setBoxHeight(0.1f)
                        .setTextX(-0.8f)
                        .setTextY(-0.93f)
                        .setText("This is a sample checkbox")
                        .setFontName(Font.SANS_SERIF)
                        .setFontStyle(Font.PLAIN)
                        .setFontSize(64)
                        .setFontColor(Color.GREEN)
                        .setCheckboxColor(Color.WHITE)
        );
        defaultScreen.createCheckBox(
                new MttCheckBox.MttCheckBoxCreateInfo()
                        .setX(-0.9f)
                        .setY(-0.75f)
                        .setWidth(0.9f)
                        .setHeight(0.1f)
                        .setBoxX(-0.9f)
                        .setBoxY(-0.75f)
                        .setBoxWidth(0.05f)
                        .setBoxHeight(0.1f)
                        .setTextX(-0.8f)
                        .setTextY(-0.78f)
                        .setText("これはチェックボックスのサンプルです")
                        .setFontName(Font.SANS_SERIF)
                        .setFontStyle(Font.PLAIN)
                        .setFontSize(64)
                        .setFontColor(Color.ORANGE)
                        .setCheckboxColor(Color.GREEN)
        );

        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
