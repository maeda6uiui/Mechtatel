package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.postprocessing.CustomizablePostProcessingNaborInfo;
import com.github.maeda6uiui.mechtatel.core.postprocessing.PostProcessingProperties;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CustomizablePostProcessingNaborTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(CustomizablePostProcessingNaborTest.class);

    public CustomizablePostProcessingNaborTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        CustomizablePostProcessingNaborTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private FreeCamera camera;

    @Override
    public void onCreate(MttWindow window) {
        URL fragShaderResource;
        try {
            fragShaderResource = Paths.get("./Mechtatel/Addon/maeda6uiui/Shader/sepia.frag").toUri().toURL();
        } catch (MalformedURLException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        var naborInfo = new CustomizablePostProcessingNaborInfo(
                Objects.requireNonNull(
                        this.getClass().getResource("./Mechtatel/Standard/Shader/PostProcessing/post_processing.vert")),
                fragShaderResource
        );
        naborInfo.setLightingType(CustomizablePostProcessingNaborInfo.LightingType.PARALLEL);

        mainScreen = window.createScreen(
                new MttScreen.MttScreenCreateInfo()
                        .setUseShadowMapping(true)
                        .setPostProcessingNaborNames(List.of("sepia"))
                        .setCustomizablePostProcessingNaborInfos(Map.of("sepia", naborInfo))
        );
        PostProcessingProperties ppProperties = mainScreen.getPostProcessingProperties();
        ppProperties.createParallelLight();
        ppProperties.fog.setStart(10.0f);
        ppProperties.fog.setEnd(20.0f);

        try {
            mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Plane/plane.obj"));
            MttModel cube = mainScreen.createModel(Paths.get("./Mechtatel/Standard/Model/Cube/cube.obj"));
            cube.translate(new Vector3f(0.0f, 3.0f, 0.0f));
        } catch (IOException e) {
            logger.error("Error", e);
            window.close();

            return;
        }

        camera = new FreeCamera(mainScreen.getCamera());
    }

    @Override
    public void onUpdate(MttWindow window) {
        camera.translate(
                window.getKeyboardPressingCount(KeyCode.W),
                window.getKeyboardPressingCount(KeyCode.S),
                window.getKeyboardPressingCount(KeyCode.A),
                window.getKeyboardPressingCount(KeyCode.D)
        );
        camera.rotate(
                window.getKeyboardPressingCount(KeyCode.UP),
                window.getKeyboardPressingCount(KeyCode.DOWN),
                window.getKeyboardPressingCount(KeyCode.LEFT),
                window.getKeyboardPressingCount(KeyCode.RIGHT)
        );

        mainScreen.draw();
        window.present(mainScreen);

        if (window.getKeyboardPressingCount(KeyCode.ENTER) == 1) {
            try {
                mainScreen.save(
                        ScreenImageType.COLOR,
                        PixelFormat.BGRA,
                        Paths.get("./screenshot.png")
                );
            } catch (IOException e) {
                logger.error("Error", e);
                window.close();
            }
        }
    }
}
