package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;

import java.awt.*;
import java.io.IOException;

public class ScrollbarTest extends Mechtatel {
    public ScrollbarTest(MttSettings settings) {
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

        new ScrollbarTest(settings);
    }

    @Override
    public void init() {
        var vScrollbar = this.createVerticalScrollbar(
                -0.9f,
                -0.9f,
                0.1f,
                1.8f,
                0.1f,
                Color.WHITE,
                Color.GRAY);
        vScrollbar.setScrollAmount(0.5f);

        var hScrollbar = this.createHorizontalScrollbar(
                -0.7f,
                -0.9f,
                1.2f,
                0.1f,
                0.1f,
                Color.WHITE,
                Color.GRAY);
        hScrollbar.setScrollAmount(0.5f);
    }
}
