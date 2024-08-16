package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.MechtatelHeadless;
import com.github.maeda6uiui.mechtatel.core.MttHeadlessInstance;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.util.Objects;

public class HeadlessModeTest extends MechtatelHeadless {
    private static final Logger logger = LoggerFactory.getLogger(HeadlessModeTest.class);

    public HeadlessModeTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        HeadlessModeTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    @Override
    public void onInit(MttHeadlessInstance instance) {
        MttScreen defaultScreen = instance.getDefaultScreen();

        try {
            defaultScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            instance.close();

            return;
        }

        defaultScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();
    }

    @Override
    public void onUpdate(MttHeadlessInstance instance) {
        MttScreen defaultScreen = instance.getDefaultScreen();
        try {
            defaultScreen.save(ScreenImageType.COLOR, PixelFormat.BGRA, Paths.get("./screenshot.png"));
        } catch (IOException e) {
            logger.error("Error", e);
        }

        instance.close();
    }
}
