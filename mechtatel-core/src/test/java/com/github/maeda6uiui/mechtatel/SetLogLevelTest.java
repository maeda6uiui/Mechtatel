package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.logging.MttLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SetLogLevelTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(SetLogLevelTest.class);

    public SetLogLevelTest(MttSettings settings) {
        super(settings);
    }

    public static void main(String[] args) {
        MttLogging.setRootLoggerLogLevel("DEBUG");
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        AnimationTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    @Override
    public void init() {
        logger.debug("init");
    }

    @Override
    public void reshape(int width, int height) {
        logger.debug("width={} height={}", width, height);
    }

    @Override
    public void dispose() {
        logger.debug("dispose");
    }
}
