package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.DrawPath;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.ScreenCreator;
import com.github.maeda6uiui.mechtatel.core.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector2f;

import java.io.IOException;

public class ShowTexture extends Mechtatel {
    public ShowTexture(MttSettings settings) {
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

        new ShowTexture(settings);
    }

    private MttScreen mainScreen;
    private MttTexturedQuad2D texturedQuad;

    @Override
    public void init() {
        var mainScreenCreator = new ScreenCreator(this, "main");
        mainScreen = mainScreenCreator.create();

        var drawPath = new DrawPath(this);
        drawPath.addToScreenDrawOrder("main");
        drawPath.setPresentScreenName("main");
        drawPath.apply();

        texturedQuad = this.createTexturedQuad2D(
                "main",
                "./Mechtatel/Standard/Model/Skybox/cubemap.png",
                new Vector2f(-1.0f, -1.0f),
                new Vector2f(1.0f, 1.0f),
                0.0f);
    }
}
