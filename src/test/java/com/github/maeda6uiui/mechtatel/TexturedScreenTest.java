package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreenContext;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.io.IOException;
import java.util.ArrayList;

public class TexturedScreenTest extends Mechtatel {
    public TexturedScreenTest(MttSettings settings) {
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

        new TexturedScreenTest(settings);
    }

    private Model3D primaryCube;
    private Model3D secondaryCube;

    private MttScreenContext primaryContext;
    private MttScreenContext secondaryContext;

    @Override
    public void init() {
        this.createScreen(
                "primary",
                2048,
                2048,
                -1,
                -1,
                true,
                null
        );
        this.createScreen(
                "secondary",
                1024,
                1024,
                512,
                512,
                false,
                null
        );

        var screenDrawOrder = new ArrayList<String>();
        screenDrawOrder.add("secondary");
        screenDrawOrder.add("primary");
        this.setScreenDrawOrder(screenDrawOrder);

        this.setScreenToPresent("primary");

        try {
            primaryCube = this.createModel3D("primary", "./Mechtatel/Model/Cube/cube.obj");
            secondaryCube = this.createModel3D("secondary", "./Mechtatel/Model/Cube/cube.obj");
        } catch (IOException e) {
            e.printStackTrace();
        }

        primaryContext = new MttScreenContext();
        secondaryContext = new MttScreenContext();

        primaryContext.setBackgroundColor(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        secondaryContext.setBackgroundColor(new Vector4f(0.0f, 1.0f, 0.0f, 1.0f));

        primaryContext.getCamera().setEye(new Vector3f(2.0f, 2.0f, 2.0f));
        secondaryContext.getCamera().setEye(new Vector3f(1.5f, 1.5f, 1.5f));

        this.updateScreenContext("primary", primaryContext);
        this.updateScreenContext("secondary", secondaryContext);
    }

    @Override
    public void preDraw(String screenName) {

    }

    @Override
    public void postDraw(String sceneName) {

    }
}
