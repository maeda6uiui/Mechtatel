package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class OverwriteDefaultShadersTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(OverwriteDefaultShadersTest.class);

    public OverwriteDefaultShadersTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttShaderConfig.load(Paths.get("./Mechtatel/shader_config.json"));
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        OverwriteDefaultShadersTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    @Override
    public void onCreate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();

        try {
            defaultScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj"));
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        defaultScreen.createLineSet().addAxes(10.0f).createBuffer();
    }

    @Override
    public void onUpdate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();

        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
