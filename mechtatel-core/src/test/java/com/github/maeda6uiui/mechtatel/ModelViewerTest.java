package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttImGui;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.joml.Vector3f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;

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
    private float[] bufScaleX;
    private float[] bufScaleY;
    private float[] bufScaleZ;

    private String modelFilepathToLoad;
    private MttModel model;

    private FreeCamera camera;

    private void resetBuffers() {
        bufScaleX[0] = bufScaleY[0] = bufScaleZ[0] = 1.0f;
    }

    @Override
    public void onInit(MttWindow initialWindow) {
        //Create ImGui instance for Mechtatel
        MttScreen defaultScreen = initialWindow.getDefaultScreen();
        imgui = defaultScreen.createImGui();

        //Add flags to ImGui IO
        imgui.makeCurrent();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        //Declare variables for ImGui inputs
        bufScaleX = new float[]{1.0f};
        bufScaleY = new float[]{1.0f};
        bufScaleZ = new float[]{1.0f};

        modelFilepathToLoad = "";

        //Draw axes
        defaultScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();

        //Create camera
        camera = new FreeCamera(defaultScreen.getCamera());
    }

    @Override
    public void onUpdate(MttWindow window) {
        //Declare ImGui components
        imgui.declare(() -> {
            boolean shouldOpenRescaleDialog = false;

            ImGui.dockSpaceOverViewport(ImGui.getMainViewport());

            //Main menu bar =====
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
                    if (ImGui.menuItem("Rescale", "", false, model != null)) {
                        shouldOpenRescaleDialog = true;
                    }

                    ImGui.endMenu();
                }

                ImGui.endMainMenuBar();
            }
            //==========

            //Open rescale dialog
            if (shouldOpenRescaleDialog) {
                ImGui.openPopup("Rescale");
            }

            //Popups =====
            if (ImGuiFileDialog.display(
                    "browse-key",
                    ImGuiWindowFlags.NoCollapse,
                    700.0f, 400.0f,
                    Float.MAX_VALUE, Float.MAX_VALUE)) {
                if (ImGuiFileDialog.isOk()) {
                    modelFilepathToLoad = ImGuiFileDialog.getFilePathName();
                }
                ImGuiFileDialog.close();
            }

            if (ImGui.beginPopupModal("Rescale", new ImBoolean(true), ImGuiWindowFlags.AlwaysAutoResize)) {
                ImGui.dragFloat("x", bufScaleX, 0.01f, 0.01f, 10.0f);
                ImGui.dragFloat("y", bufScaleY, 0.01f, 0.01f, 10.0f);
                ImGui.dragFloat("z", bufScaleZ, 0.01f, 0.01f, 10.0f);
                ImGui.newLine();

                if (ImGui.button("Cancel")) {
                    ImGui.closeCurrentPopup();
                    this.resetBuffers();
                }
                ImGui.sameLine();
                if (ImGui.button("OK")) {
                    if (model != null) {
                        model.rescale(new Vector3f(bufScaleX[0], bufScaleY[0], bufScaleZ[0]));
                    }

                    ImGui.closeCurrentPopup();
                    this.resetBuffers();
                }

                ImGui.endPopup();
            }
            //==========
        });

        //Load model
        MttScreen defaultScreen = window.getDefaultScreen();
        if (!modelFilepathToLoad.isEmpty() && Files.exists(Paths.get(modelFilepathToLoad))) {
            if (model != null) {
                model.cleanup();
            }

            try {
                var modelURL = Paths.get(modelFilepathToLoad).toUri().toURL();
                model = defaultScreen.createModel(modelURL);
            } catch (IOException | URISyntaxException e) {
                logger.error("Error", e);
                window.close();

                return;
            }

            modelFilepathToLoad = "";
        }

        //Translate and rotate camera according to key input
        camera.translate(
                window.getKeyboardPressingCount(KeyCode.W),
                window.getKeyboardPressingCount(KeyCode.S),
                window.getKeyboardPressingCount(KeyCode.A),
                window.getKeyboardPressingCount(KeyCode.D)
        );
        camera.rotate(
                window.getKeyboardPressingCount(KeyCode.UP),
                window.getKeyboardPressingCount(KeyCode.DOWN),
                window.getKeyboardPressingCount(KeyCode.LEFT),
                window.getKeyboardPressingCount(KeyCode.RIGHT)
        );

        //Run rendering
        defaultScreen.draw();

        //Present to the window
        window.present(defaultScreen);
    }
}
