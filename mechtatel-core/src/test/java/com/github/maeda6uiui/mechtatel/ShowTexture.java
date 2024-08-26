package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.nio.file.Paths;

public class ShowTexture extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(ShowTexture.class);

    public ShowTexture(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ShowTexture::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    @Override
    public void onCreate(MttWindow window) {
        try {
            window.getDefaultScreen().createTexturedQuad2D(
                    Paths.get("./Mechtatel/Standard/Texture/checker.png"),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f
            );
        } catch (FileNotFoundException e) {
            logger.error("Error", e);
            window.close();
        }
    }

    @Override
    public void onUpdate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
