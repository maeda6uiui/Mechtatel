package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.logging.MttLogging;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WindowModeTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(WindowModeTest.class);

    public WindowModeTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttLogging.setRootLoggerLogLevel("DEBUG");

        var settings = new MttSettings();
        settings.windowSettings.fullScreen = true;
        //settings.windowSettings.windowedFullScreen=true;
        settings.windowSettings.monitorIndex = 0;

        new WindowModeTest(settings);
    }

    @Override
    public void onCreate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.createLineSet().addAxes(10.0f).createBuffer();
    }

    @Override
    public void onUpdate(MttWindow window) {
        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);

        if (window.getKeyboardPressingCount(KeyCode.ESCAPE) == 1) {
            window.close();
        }
    }
}
