package com.github.maeda6uiui.mechtatel.hello;

import com.github.maeda6uiui.mechtatel.core.MttHeadlessInstance;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Renders a monochrome image.
 * Monochrome filter is written in Slang, so this class should work as a test case
 * to see whether Slang compilation functions correctly on your machine.
 *
 * @author maeda6uiui
 */
class MttMonochromeImage implements IMttHello {
    private static final Logger logger = LoggerFactory.getLogger(MttMonochromeImage.class);

    private MttScreen mainScreen;

    @Override
    public void onInit(MttHeadlessInstance instance) {
        //Get settings
        MttSettings settings = MttSettings.get().orElse(new MttSettings());

        mainScreen = instance.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setFullScreenEffectNaborNames(List.of("fse.monochrome_effect"))
                        .setScreenWidth(settings.headlessSettings.width)
                        .setScreenHeight(settings.headlessSettings.height)
        );

        try {
            mainScreen.createTexturedQuad2D(
                    Paths.get("./Data/nastya.jpg"),
                    new Vector2f(-0.9f, -0.9f),
                    new Vector2f(0.9f, 0.9f),
                    0.0f
            );
        } catch (IOException e) {
            logger.error("Error while loading an image", e);
            instance.close();
        }
    }

    @Override
    public void onUpdate(MttHeadlessInstance instance, Path imageOutputPath) {
        mainScreen.draw();

        try {
            mainScreen.save(ScreenImageType.COLOR, PixelFormat.BGRA, imageOutputPath);
        } catch (IOException e) {
            logger.error("Error while saving image to disk", e);
        } finally {
            instance.close();
        }
    }
}

