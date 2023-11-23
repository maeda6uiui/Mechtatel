package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.DrawPath;
import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.ScreenCreator;
import com.github.maeda6uiui.mechtatel.core.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.joml.Vector2f;

public class ShowTexture extends Mechtatel {
    public ShowTexture(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ShowTexture::new,
                        () -> {
                            System.out.println("Failed to load settings");
                        }
                );
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
                "./Mechtatel/Standard/Texture/checker.png",
                new Vector2f(-1.0f, -1.0f),
                new Vector2f(1.0f, 1.0f),
                0.0f);
    }
}
