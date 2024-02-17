package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttImGui;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.flag.ImGuiConfigFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ModelViewerTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(ModelViewerTest.class);

    public ModelViewerTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ModelViewerTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttImGui imgui;

    @Override
    public void onInit(MttWindow initialWindow) {
        MttScreen defaultScreen = initialWindow.getDefaultScreen();
        imgui = defaultScreen.createImGui();

        imgui.makeCurrent();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        ImGui.styleColorsLight();
    }

    @Override
    public void onUpdate(MttWindow window) {
        imgui.declare(() -> {
            ImGui.dockSpaceOverViewport(ImGui.getMainViewport());

            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("File")) {
                    ImGui.menuItem("Open...", "Ctrl+O");
                    ImGui.separator();
                    if (ImGui.menuItem("Exit", "Alt+F4")) {
                        window.close();
                    }
                    
                    ImGui.endMenu();
                }
                if (ImGui.beginMenu("Tools")) {
                    ImGui.menuItem("Rescale");

                    ImGui.endMenu();
                }

                ImGui.endMainMenuBar();
            }
        });

        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
