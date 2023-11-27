package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttHorizontalScrollbar;
import com.github.maeda6uiui.mechtatel.core.component.gui.MttVerticalScrollbar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

public class ScrollbarTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(ScrollbarTest.class);

    public ScrollbarTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ScrollbarTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    @Override
    public void init() {
        var vScrollbar = this.createVerticalScrollbar(
                new MttVerticalScrollbar.MttVerticalScrollbarCreateInfo()
                        .setX(-0.9f)
                        .setY(-0.9f)
                        .setWidth(0.1f)
                        .setHeight(1.8f)
                        .setGrabHeight(0.1f)
                        .setFrameColor(Color.WHITE)
                        .setGrabFrameColor(Color.GRAY)
        );
        vScrollbar.setScrollAmount(0.5f);

        var hScrollbar = this.createHorizontalScrollbar(
                new MttHorizontalScrollbar.MttHorizontalScrollbarCreateInfo()
                        .setX(-0.7f)
                        .setY(-0.9f)
                        .setWidth(1.2f)
                        .setHeight(0.1f)
                        .setGrabWidth(0.1f)
                        .setFrameColor(Color.WHITE)
                        .setGrabFrameColor(Color.GRAY)
        );
        hScrollbar.setScrollAmount(0.5f);
    }
}
