package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.*;
import com.github.maeda6uiui.mechtatel.core.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.net.URISyntaxException;
import java.util.Objects;

public class ShowTexture extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(ShowTexture.class);

    public ShowTexture(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ShowTexture::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private MttTexturedQuad2D texturedQuad;

    @Override
    public void init(MttWindow window) {
        var mainScreenCreator = new ScreenCreator(window, "main");
        mainScreen = mainScreenCreator.create();

        var drawPath = new DrawPath(window);
        drawPath.addToScreenDrawOrder("main");
        drawPath.setPresentScreenName("main");
        drawPath.apply();

        try {
            texturedQuad = window.createTexturedQuad2D(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Texture/checker.png")),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f);
        } catch (URISyntaxException | FileNotFoundException e) {
            logger.error("Error", e);
            window.close();
        }
    }
}
