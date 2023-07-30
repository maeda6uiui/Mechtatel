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

import java.io.IOException;

public class FlexibleNaborTest extends Mechtatel {
    public FlexibleNaborTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = MttSettings.load("./Mechtatel/Setting/settings.json");
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new FlexibleNaborTest(settings);
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

        var naborInfo = new FlexibleNaborInfo(
                "./Mechtatel/Standard/Shader/PostProcessing/post_processing.vert",
                "./Mechtatel/Addon/maeda6uiui/Shader/sepia.frag");
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
            plane = this.createModel3D("main", "./Mechtatel/Standard/Model/Plane/plane.obj");
            teapot = this.createModel3D("main", "./Mechtatel/Standard/Model/Teapot/teapot.obj");
            cube = this.createModel3D("main", "./Mechtatel/Standard/Model/Cube/cube.obj");
        } catch (IOException e) {
            e.printStackTrace();
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
                e.printStackTrace();
            }
        }
    }
}
