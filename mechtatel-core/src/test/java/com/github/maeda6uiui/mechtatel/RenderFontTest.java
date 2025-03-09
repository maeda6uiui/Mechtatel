package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttFont;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class RenderFontTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(RenderFontTest.class);

    public RenderFontTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        RenderFontTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    @Override
    public void onCreate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();
        MttFont font = defaultScreen.createFont(
                new Font(Font.SERIF, Font.PLAIN, 128),
                true,
                Color.GREEN,
                "こんにちは"
        );
        font.prepare("こんにちは", new Vector2f(-1.0f));
        font.createBuffers();
    }

    @Override
    public void onUpdate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
