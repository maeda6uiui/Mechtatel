package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectProperties;
import com.github.maeda6uiui.mechtatel.core.fseffect.GaussianBlurInfo;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector2f;
import org.joml.Vector2i;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class GaussianBlurTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(GaussianBlurTest.class);

    public GaussianBlurTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        GaussianBlurTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;

    @Override
    public void onCreate(MttWindow window) {
        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setFullScreenEffectNaborNames(List.of("gaussian_blur"))
        );

        try {
            mainScreen.createTexturedQuad2D(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Texture/checker.png")),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f
            );
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        var gaussianBlurInfo = new GaussianBlurInfo(32, 16.0f, 0.1f);
        gaussianBlurInfo.setTextureSize(new Vector2i(mainScreen.getScreenWidth(), mainScreen.getScreenHeight()));

        FullScreenEffectProperties fseProperties = mainScreen.getFullScreenEffectProperties();
        fseProperties.gaussianBlurInfo = gaussianBlurInfo;

        logger.info(Arrays.toString(gaussianBlurInfo.getWeights()));
    }

    @Override
    public void onUpdate(MttWindow window) {
        mainScreen.draw();
        window.present(mainScreen);
    }
}
