package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.DrawPath;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.ScreenCreator;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.nabor.FlexibleNaborInfo;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Objects;

public class FlexibleNaborTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(FlexibleNaborTest.class);

    public FlexibleNaborTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        FlexibleNaborTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private MttModel plane;
    private MttModel teapot;
    private MttModel cube;
    private FreeCamera camera;

    @Override
    public void init() {
        var screenCreator = new ScreenCreator(this, "main");
        screenCreator.setUseShadowMapping(true);
        screenCreator.addPostProcessingNabor("sepia");

        URL fragShaderResource;
        try {
            fragShaderResource = Paths.get("./Mechtatel/Addon/maeda6uiui/Shader/sepia.frag").toUri().toURL();
        } catch (MalformedURLException e) {
            logger.error("Error", e);
            this.closeWindow();

            return;
        }

        var naborInfo = new FlexibleNaborInfo(
                Objects.requireNonNull(this.getClass().getResource(
                        "/Standard/Shader/PostProcessing/post_processing.vert")),
                fragShaderResource
        );
        naborInfo.setLightingType("parallel_light");
        screenCreator.addFlexibleNaborInfo("sepia", naborInfo);

        mainScreen = screenCreator.create();

        mainScreen.createParallelLight();
        mainScreen.getFog().setStart(10.0f);
        mainScreen.getFog().setEnd(20.0f);

        var drawPath = new DrawPath(this);
        drawPath.addToScreenDrawOrder("main");
        drawPath.setPresentScreenName("main");
        drawPath.apply();

        try {
            plane = this.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Plane/plane.obj"))
            );
            teapot = this.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Teapot/teapot.obj"))
            );
            cube = this.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            this.closeWindow();

            return;
        }

        cube.translate(new Vector3f(0.0f, 3.0f, 0.0f));

        camera = new FreeCamera(mainScreen.getCamera());
    }

    @Override
    public void update() {
        camera.translate(
                this.getKeyboardPressingCount("W"),
                this.getKeyboardPressingCount("S"),
                this.getKeyboardPressingCount("A"),
                this.getKeyboardPressingCount("D")
        );
        camera.rotate(
                this.getKeyboardPressingCount("UP"),
                this.getKeyboardPressingCount("DOWN"),
                this.getKeyboardPressingCount("LEFT"),
                this.getKeyboardPressingCount("RIGHT")
        );
    }

    @Override
    public void postPresent() {
        if (this.getKeyboardPressingCount("ENTER") == 1) {
            try {
                this.saveScreenshot("main", "bgra", "screenshot.png");
            } catch (IOException e) {
                logger.error("Error", e);
                this.closeWindow();
            }
        }
    }
}
