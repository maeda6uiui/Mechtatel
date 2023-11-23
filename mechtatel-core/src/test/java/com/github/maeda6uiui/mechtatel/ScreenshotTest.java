package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.DrawPath;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.ScreenCreator;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.component.MttSphere;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

public class ScreenshotTest extends Mechtatel {
    public ScreenshotTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ScreenshotTest::new,
                        () -> {
                            System.out.println("Failed to load settings");
                        }
                );
    }

    private FreeCamera camera;

    private MttModel plane;
    private MttModel cube;
    private MttSphere sphere;

    @Override
    public void init() {
        var screenCreator = new ScreenCreator(this, "main");
        screenCreator.addPostProcessingNabor("parallel_light");
        screenCreator.addPostProcessingNabor("fog");
        screenCreator.setUseShadowMapping(true);
        var mainScreen = screenCreator.create();

        mainScreen.createParallelLight();
        mainScreen.getFog().setStart(10.0f);
        mainScreen.getFog().setEnd(20.0f);

        camera = new FreeCamera(mainScreen.getCamera());

        var drawPath = new DrawPath(this);
        drawPath.addToScreenDrawOrder("main");
        drawPath.setPresentScreenName("main");
        drawPath.apply();

        try {
            plane = this.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Plane/plane.obj"))
            );
            cube = this.createModel(
                    "main",
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            this.closeWindow();

            return;
        }

        cube.translate(new Vector3f(0.0f, 1.0f, 0.0f));

        sphere = this.createSphere(
                new Vector3f(5.0f, 2.0f, 0.0f),
                2.0f, 32, 32, new Vector4f(1.0f, 0.0f, 1.0f, 1.0f)
        );
    }

    @Override
    public void dispose() {

    }

    @Override
    public void reshape(int width, int height) {

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
                e.printStackTrace();
                this.closeWindow();
            }
        }
    }
}
