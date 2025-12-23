package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.MechtatelHeadless;
import com.github.maeda6uiui.mechtatel.core.MttHeadlessInstance;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttImGui;
import imgui.ImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Paths;

public class ImGuiHeadlessTest extends MechtatelHeadless {
    private static final Logger logger = LoggerFactory.getLogger(ImGuiHeadlessTest.class);

    public ImGuiHeadlessTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ImGuiHeadlessTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttImGui imgui;

    @Override
    public void onInit(MttHeadlessInstance instance) {
        MttScreen defaultScreen = instance.getDefaultScreen();
        imgui = defaultScreen.createImGui();
    }

    @Override
    public void onUpdate(MttHeadlessInstance instance) {
        imgui.declare(ImGui::showDemoWindow);

        MttScreen defaultScreen = instance.getDefaultScreen();
        defaultScreen.draw();

        try {
            defaultScreen.save(ScreenImageType.COLOR, PixelFormat.BGRA, Paths.get("./screenshot.png"));
        } catch (IOException e) {
            logger.error("Error", e);
        }

        instance.close();
    }
}
