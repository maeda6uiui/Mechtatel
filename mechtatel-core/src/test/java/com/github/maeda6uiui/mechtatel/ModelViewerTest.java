package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.camera.FreeCamera;
import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;
import com.github.maeda6uiui.mechtatel.core.operation.BiTextureOperation;
import com.github.maeda6uiui.mechtatel.core.operation.BiTextureOperationParameters;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttImGui;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttModel;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttTexturedQuad2D;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.extension.imguifiledialog.ImGuiFileDialog;
import imgui.extension.imguifiledialog.flag.ImGuiFileDialogFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImBoolean;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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

    private MttScreen imguiScreen;
    private MttScreen mainScreen;
    private MttScreen finalScreen;
    private BiTextureOperation opAdd;
    private MttTexturedQuad2D texturedQuad;
    private boolean shouldShowMainObjects;

    private MttImGui imgui;

    //Buffers for ImGui inputs =====
    private float[] bufTranslateX;
    private float[] bufTranslateY;
    private float[] bufTranslateZ;
    private float[] bufRotateX;
    private float[] bufRotateY;
    private float[] bufRotateZ;
    private float[] bufScaleX;
    private float[] bufScaleY;
    private float[] bufScaleZ;
    //==========

    private String modelFilepathToLoad;
    private MttModel model;

    private FreeCamera camera;

    private void resetTranslateBuffers() {
        bufTranslateX[0] = bufTranslateY[0] = bufTranslateZ[0] = 0.0f;
    }

    private void resetRotateBuffers() {
        bufRotateX[0] = bufRotateY[0] = bufRotateZ[0] = 0.0f;
    }

    private void resetScaleBuffers() {
        bufScaleX[0] = bufScaleY[0] = bufScaleZ[0] = 1.0f;
    }

    @Override
    public void onInit(MttWindow initialWindow) {
        //Create screens
        imguiScreen = initialWindow.createScreen(new MttScreen.MttScreenCreateInfo());
        mainScreen = initialWindow.createScreen(new MttScreen.MttScreenCreateInfo());
        finalScreen = initialWindow.createScreen(new MttScreen.MttScreenCreateInfo());

        try {
            //Create a textured quad to render to final screen
            //Texture specified here will be replaced later
            texturedQuad = finalScreen.createTexturedQuad2D(
                    Objects.requireNonNull(this.getClass().getResource("/Standard/Texture/checker.png")),
                    new Vector2f(-1.0f, -1.0f),
                    new Vector2f(1.0f, 1.0f),
                    0.0f
            );
        } catch (IOException | URISyntaxException e) {
            logger.error("Error", e);
            initialWindow.close();

            return;
        }

        //Set flag to represent whether to show main objects
        shouldShowMainObjects = true;

        //Create ImGui instance for Mechtatel
        imgui = imguiScreen.createImGui();

        //Add flags to ImGui IO
        imgui.makeCurrent();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        //Declare variables for ImGui inputs
        bufTranslateX = new float[1];
        bufTranslateY = new float[1];
        bufTranslateZ = new float[1];
        this.resetTranslateBuffers();

        bufRotateX = new float[1];
        bufRotateY = new float[1];
        bufRotateZ = new float[1];
        this.resetRotateBuffers();

        bufScaleX = new float[1];
        bufScaleY = new float[1];
        bufScaleZ = new float[1];
        this.resetScaleBuffers();

        modelFilepathToLoad = "";

        //Draw axes
        mainScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();

        //Create camera
        camera = new FreeCamera(mainScreen.getCamera());

        //Create texture operations
        this.createTextureOperations();
    }

    @Override
    public void onRecreate(MttWindow window, int width, int height) {
        //Texture operations must be recreated on resource recreation accompanied by window resize,
        //as some resources such as underlying textures of a screen are destroyed and no longer valid.
        this.createTextureOperations();
    }

    @Override
    public void onUpdate(MttWindow window) {
        //Declare ImGui components
        imgui.declare(() -> {
            boolean shouldOpenTranslateDialog = false;
            boolean shouldOpenRotateDialog = false;
            boolean shouldOpenRescaleDialog = false;

            //Main menu bar =====
            if (ImGui.beginMainMenuBar()) {
                if (ImGui.beginMenu("File")) {
                    if (ImGui.menuItem("Open...")) {
                        ImGuiFileDialog.openModal(
                                "OpenFile", "Open", ".bd1,.obj", ".",
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
                    if (ImGui.menuItem("Translate", "", false, model != null)) {
                        shouldOpenTranslateDialog = true;
                    }
                    if (ImGui.menuItem("Rotate", "", false, model != null)) {
                        shouldOpenRotateDialog = true;
                    }
                    if (ImGui.menuItem("Rescale", "", false, model != null)) {
                        shouldOpenRescaleDialog = true;
                    }

                    ImGui.endMenu();
                }

                ImGui.endMainMenuBar();
            }
            //==========

            //Open dialogs =====
            if (shouldOpenTranslateDialog) {
                ImGui.openPopup("Translate");
            }
            if (shouldOpenRotateDialog) {
                ImGui.openPopup("Rotate");
            }
            if (shouldOpenRescaleDialog) {
                ImGui.openPopup("Rescale");
            }
            //==========

            //Dialogs =====
            if (ImGuiFileDialog.display(
                    "OpenFile",
                    ImGuiWindowFlags.NoCollapse,
                    700.0f, 400.0f,
                    Float.MAX_VALUE, Float.MAX_VALUE)) {
                if (ImGuiFileDialog.isOk()) {
                    modelFilepathToLoad = ImGuiFileDialog.getFilePathName();
                }
                ImGuiFileDialog.close();
            }

            if (ImGui.beginPopupModal("Translate", new ImBoolean(true), ImGuiWindowFlags.AlwaysAutoResize)) {
                ImGui.dragFloat("x", bufTranslateX, 0.1f, -100.0f, 100.0f);
                ImGui.dragFloat("y", bufTranslateY, 0.1f, -100.0f, 100.0f);
                ImGui.dragFloat("z", bufTranslateZ, 0.1f, -100.0f, 100.0f);
                ImGui.newLine();

                if (ImGui.button("Cancel")) {
                    ImGui.closeCurrentPopup();
                    this.resetTranslateBuffers();
                }
                ImGui.sameLine();
                if (ImGui.button("OK")) {
                    if (model != null) {
                        model.translate(new Vector3f(bufTranslateX[0], bufTranslateY[0], bufTranslateZ[0]));
                    }

                    ImGui.closeCurrentPopup();
                    this.resetTranslateBuffers();
                }

                ImGui.endPopup();
            }
            if (ImGui.beginPopupModal("Rotate", new ImBoolean(true), ImGuiWindowFlags.AlwaysAutoResize)) {
                ImGui.dragFloat("x", bufRotateX, 0.01f, (float) Math.PI * (-0.5f), (float) Math.PI * 0.5f);
                ImGui.dragFloat("y", bufRotateY, 0.01f, (float) Math.PI * (-0.5f), (float) Math.PI * 0.5f);
                ImGui.dragFloat("z", bufRotateZ, 0.01f, (float) Math.PI * (-0.5f), (float) Math.PI * 0.5f);
                ImGui.newLine();

                if (ImGui.button("Cancel")) {
                    ImGui.closeCurrentPopup();
                    this.resetRotateBuffers();
                }
                ImGui.sameLine();
                if (ImGui.button("OK")) {
                    if (model != null) {
                        model.rotX(bufRotateX[0]).rotY(bufRotateY[0]).rotZ(bufRotateZ[0]);
                    }

                    ImGui.closeCurrentPopup();
                    this.resetRotateBuffers();
                }

                ImGui.endPopup();
            }
            if (ImGui.beginPopupModal("Rescale", new ImBoolean(true), ImGuiWindowFlags.AlwaysAutoResize)) {
                ImGui.dragFloat("x", bufScaleX, 0.01f, 0.01f, 10.0f);
                ImGui.dragFloat("y", bufScaleY, 0.01f, 0.01f, 10.0f);
                ImGui.dragFloat("z", bufScaleZ, 0.01f, 0.01f, 10.0f);
                ImGui.newLine();

                if (ImGui.button("Cancel")) {
                    ImGui.closeCurrentPopup();
                    this.resetScaleBuffers();
                }
                ImGui.sameLine();
                if (ImGui.button("OK")) {
                    if (model != null) {
                        model.rescale(new Vector3f(bufScaleX[0], bufScaleY[0], bufScaleZ[0]));
                    }

                    ImGui.closeCurrentPopup();
                    this.resetScaleBuffers();
                }

                ImGui.endPopup();
            }
            //==========
        });

        //Load model
        if (!modelFilepathToLoad.isEmpty() && Files.exists(Paths.get(modelFilepathToLoad))) {
            if (model != null) {
                model.cleanup();
            }

            try {
                var modelURL = Paths.get(modelFilepathToLoad).toUri().toURL();
                model = mainScreen.createModel(modelURL);
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

        //Update flag to show main objects
        if (window.getKeyboardPressingCount(KeyCode.F1) == 1) {
            shouldShowMainObjects = !shouldShowMainObjects;
        }

        //Rendering
        imguiScreen.draw();
        mainScreen.draw();

        //Filter out main rendering if flag is set to false
        if (!shouldShowMainObjects) {
            opAdd.getBiParameters().setSecondTextureFactor(new Vector4f(0.0f, 0.0f, 0.0f, 1.0f));
        } else {
            opAdd.getBiParameters().setSecondTextureFactor(new Vector4f(1.0f, 1.0f, 1.0f, 1.0f));
        }

        //Add rendering results
        opAdd.run();

        //Render to the final screen and present it
        finalScreen.draw();
        window.present(finalScreen);
    }

    private void createTextureOperations() {
        //Clean up texture operations if there is any
        if (opAdd != null) {
            opAdd.cleanup();
        }

        //Add rendering results of ImGui and main screens
        MttTexture imguiColorTexture = imguiScreen.texturize(ScreenImageType.COLOR, finalScreen);
        MttTexture mainColorTexture = mainScreen.texturize(ScreenImageType.COLOR, finalScreen);

        opAdd = finalScreen.createBiTextureOperation(
                Arrays.asList(imguiColorTexture, mainColorTexture),
                new ArrayList<>(),
                true
        );

        var texOpAddParams = new BiTextureOperationParameters();
        texOpAddParams.setOperationType(BiTextureOperationParameters.OperationType.ADD);
        opAdd.setBiParameters(texOpAddParams);

        //Set result texture of add operation as final output
        texturedQuad.replaceTexture(opAdd.getResultTexture());
    }
}
