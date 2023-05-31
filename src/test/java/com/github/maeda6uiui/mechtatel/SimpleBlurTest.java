package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.DrawPath;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.ScreenCreator;
import com.github.maeda6uiui.mechtatel.core.blur.SimpleBlurInfo;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.component.MttModel3D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;

import java.io.IOException;

public class SimpleBlurTest extends Mechtatel {
    public SimpleBlurTest(MttSettings settings) {
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

        new SimpleBlurTest(settings);
    }

    private MttScreen mainScreen;
    private MttModel3D mainModel;
    private FreeCamera camera;

    @Override
    public void init() {
        var mainScreenCreator = new ScreenCreator(this, "main");
        mainScreenCreator.addPostProcessingNabor("simple_blur");
        mainScreen = mainScreenCreator.create();

        var drawPath = new DrawPath(this);
        drawPath.addToScreenDrawOrder("main");
        drawPath.setPresentScreenName("main");
        drawPath.apply();

        try {
            mainModel = this.createModel3D("main", "./Mechtatel/Standard/Model/Cube/cube.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        camera = new FreeCamera(mainScreen.getCamera());
    }

    @Override
    public void update() {
        var simpleBlurInfo = new SimpleBlurInfo();
        simpleBlurInfo.setTextureWidth(mainScreen.getScreenWidth());
        simpleBlurInfo.setTextureHeight(mainScreen.getScreenHeight());
        simpleBlurInfo.setBlurSize(10);
        simpleBlurInfo.setStride(1);
        mainScreen.setSimpleBlurInfo(simpleBlurInfo);

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
