package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttImGui;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import imgui.ImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Objects;

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
    private MttModel box;

    @Override
    public void onInit(MttWindow initialWindow) {
        MttScreen defaultScreen = initialWindow.getDefaultScreen();
        imgui = defaultScreen.createImGui();

        try {
            box = defaultScreen.createModel(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Model/Cube/cube.obj"))
            );
        } catch (URISyntaxException | IOException e) {
            logger.error("Error", e);
            initialWindow.close();
        }
    }

    @Override
    public void onUpdate(MttWindow window) {
        imgui.declare(() -> {
            if (ImGui.begin("Window")) {
                ImGui.text("Hello, world!");
            }
            ImGui.end();
        });

        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
