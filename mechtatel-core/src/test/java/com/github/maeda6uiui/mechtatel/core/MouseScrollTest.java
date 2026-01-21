package com.github.maeda6uiui.mechtatel.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MouseScrollTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(MouseScrollTest.class);

    public MouseScrollTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        MouseScrollTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    @Override
    public void onInit(MttWindow initialWindow) {
        initialWindow.setScrollCallback((dx, dy) -> {
            logger.info("Scroll: dx={}, dy={}", dx, dy);
        });
    }
}
