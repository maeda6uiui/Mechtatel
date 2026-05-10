package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OutOfBoundsTextureAllocationIndexTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(OutOfBoundsTextureAllocationIndexTest.class);

    public OutOfBoundsTextureAllocationIndexTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        OutOfBoundsTextureAllocationIndexTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    @Override
    public void onCreate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();

        //This should throw an exception.
        defaultScreen.getVulkanScreen().deallocateTexture(-1);
    }
}
