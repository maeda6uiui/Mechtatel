package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.ScreenCreator;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel3D;
import com.github.maeda6uiui.mechtatel.core.nabor.FlexibleNaborInfo;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;

import java.io.IOException;

public class FlexibleNaborTest extends Mechtatel {
    public FlexibleNaborTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings settings;
        try {
            settings = new MttSettings("./Mechtatel/Setting/settings.json");
        } catch (IOException e) {
            System.out.println("Failed to load setting file. Use default settings");
            settings = new MttSettings();
        }

        new FlexibleNaborTest(settings);
    }

    private MttScreen mainScreen;
    private MttModel3D plane;
    private MttModel3D teapot;
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

        try {
            plane = this.createModel3D("main", "./Mechtatel/Standard/Model/Plane/plane.obj");
            teapot = this.createModel3D("main", "./Mechtatel/Standard/Model/Teapot/teapot.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

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
}
