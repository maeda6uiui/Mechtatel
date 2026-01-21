package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttTexturedQuad2D;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class MonochromeEffectTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(MonochromeEffectTest.class);

    public MonochromeEffectTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        MonochromeEffectTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private MttTexturedQuad2D texturedQuad;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setFullScreenEffectNaborNames(List.of("fse.monochrome_effect"))
        );

        try {
            texturedQuad = mainScreen.createTexturedQuad2D(
                    Paths.get("./Mechtatel/Standard/Image/nastya.jpg"),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f
            );
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }
    }

    @Override
    public void onUpdate(MttWindow window) {
        mainScreen.draw();
        window.present(mainScreen);
    }
}
