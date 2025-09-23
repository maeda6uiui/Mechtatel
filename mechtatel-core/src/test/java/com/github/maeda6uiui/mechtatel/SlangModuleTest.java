package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.fseffect.FullScreenEffectNaborInfo;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.util.MttURLUtils;
import org.joml.Vector2f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class SlangModuleTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(SlangModuleTest.class);

    public SlangModuleTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        SlangModuleTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private MttTexturedQuad2D texturedQuad;

    @Override
    public void onCreate(MttWindow window) {
        URL vertShaderResource;
        List<URL> fragShaderResources;
        try {
            vertShaderResource = MttURLUtils.getResourceURL(
                    "/Standard/Shader/FullScreenEffect/full_screen_effect.vert.slang",
                    Mechtatel.class
            );
            fragShaderResources = MttURLUtils.getMatchedResourceURLs(
                    Paths.get("./Mechtatel/Addon/maeda6uiui/Shader/one_bit_filtering"),
                    FileSystems.getDefault().getPathMatcher("glob:**.slang")
            );
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        var naborInfo = new FullScreenEffectNaborInfo(
                List.of(vertShaderResource),
                List.of(fragShaderResources.get(0), fragShaderResources.get(1)));

        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setFullScreenEffectNaborNames(List.of("fse.one_bit_filtering"))
                        .setFullScreenEffectNaborInfos(Map.of("fse.one_bit_filtering", naborInfo))
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
