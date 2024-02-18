package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttImGui;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.flag.ImGuiConfigFlags;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
    private Map<String, String> selections;

    @Override
    public void onInit(MttWindow initialWindow) {
        MttScreen defaultScreen = initialWindow.getDefaultScreen();
        imgui = defaultScreen.createImGui();

        imgui.makeCurrent();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        ImGui.styleColorsLight();

        selections = new HashMap<>();
    }

    @Override
    public void onUpdate(MttWindow window) {
        imgui.declare(() -> {
            ImGui.dockSpaceOverViewport(ImGui.getMainViewport());

            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("File")) {
                    if (ImGui.menuItem("Open...", "Ctrl+O")) {
                        ImGuiFileDialog.openModal(
                                "browse-key",
                                "Open",
                                ".bd1,.obj",
                                ".",
                                null,
                                10.0f,
                                1,
                                0,
                                ImGuiFileDialogFlags.None
                        );
                    }
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

            if (ImGuiFileDialog.display("browse-key", ImGuiFileDialogFlags.None, 700.0f, 400.0f, Float.MAX_VALUE, Float.MAX_VALUE)) {
                if (ImGuiFileDialog.isOk()) {
                    selections.putAll(ImGuiFileDialog.getSelection());
                }
                ImGuiFileDialog.close();
            }
        });

        if (!selections.isEmpty()) {
            logger.info("Selections=");
            selections.forEach((k, v) -> logger.info("key={},value={}", k, v));
            selections.clear();
        }

        MttScreen defaultScreen = window.getDefaultScreen();
        defaultScreen.draw();
        window.present(defaultScreen);
    }
}
