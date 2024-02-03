package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImGuiTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(ImGuiTest.class);

    public ImGuiTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ImGuiTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttImGui imgui;

    @Override
    public void onInit(MttWindow initialWindow) {
        MttScreen defaultScreen = initialWindow.getDefaultScreen();
        imgui = defaultScreen.createImGui();
    }

    @Override
    public void onUpdate(MttWindow window) {

    }
}
