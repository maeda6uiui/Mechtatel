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
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
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

    //Selected filepaths
    private Map<String, String> selections;

    //Buffers for values associated with ImGui controls
    private float[] bufScaleX;
    private float[] bufScaleY;
    private float[] bufScaleZ;

    private void resetBuffers() {
        bufScaleX[0] = bufScaleY[0] = bufScaleZ[0] = 1.0f;
    }

    @Override
    public void onInit(MttWindow initialWindow) {
        MttScreen defaultScreen = initialWindow.getDefaultScreen();
        imgui = defaultScreen.createImGui();

        imgui.makeCurrent();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        ImGui.styleColorsLight();

        selections = new HashMap<>();

        bufScaleX = new float[]{1.0f};
        bufScaleY = new float[]{1.0f};
        bufScaleZ = new float[]{1.0f};
    }

    @Override
    public void onUpdate(MttWindow window) {
        imgui.declare(() -> {
            boolean shouldOpenRescaleDialog = false;

            ImGui.dockSpaceOverViewport(ImGui.getMainViewport());

            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("File")) {
                    if (ImGui.menuItem("Open...")) {
                        ImGuiFileDialog.openModal(
                                "browse-key", "Open", ".bd1,.obj", ".",
                                null,
                                10.0f, 1, 0,
                                ImGuiFileDialogFlags.DisableCreateDirectoryButton
                        );
                    }
                    ImGui.separator();
                    if (ImGui.menuItem("Exit")) {
                        window.close();
                    }

                    ImGui.endMenu();
                }
                if (ImGui.beginMenu("Tools")) {
                    if (ImGui.menuItem("Rescale")) {
                        shouldOpenRescaleDialog = true;
                    }

                    ImGui.endMenu();
                }

                ImGui.endMainMenuBar();
            }

            if (ImGuiFileDialog.display(
                    "browse-key",
                    ImGuiWindowFlags.NoCollapse,
                    700.0f, 400.0f,
                    Float.MAX_VALUE, Float.MAX_VALUE)) {
                if (ImGuiFileDialog.isOk()) {
                    selections.putAll(ImGuiFileDialog.getSelection());
                }
                ImGuiFileDialog.close();
            }

            if (shouldOpenRescaleDialog) {
                ImGui.openPopup("Rescale");
            }
            if (ImGui.beginPopupModal("Rescale", new ImBoolean(true), ImGuiWindowFlags.AlwaysAutoResize)) {
                ImGui.sliderFloat("x", bufScaleX, 0.01f, 10.0f);
                ImGui.sliderFloat("y", bufScaleY, 0.01f, 10.0f);
                ImGui.sliderFloat("z", bufScaleZ, 0.01f, 10.0f);
                ImGui.newLine();

                if (ImGui.button("Cancel")) {
                    ImGui.closeCurrentPopup();
                    this.resetBuffers();
                }
                ImGui.sameLine();
                if (ImGui.button("OK")) {
                    ImGui.closeCurrentPopup();
                    this.resetBuffers();
                }

                ImGui.endPopup();
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
