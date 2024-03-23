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

    private MttImGui imgui;
    private float[] bufScaleX;
    private float[] bufScaleY;
    private float[] bufScaleZ;

    private String modelFilepathToLoad;
    private MttModel model;

    private MttScreen imguiScreen;
    private MttScreen mainScreen;
    private MttScreen finalScreen;
    private BiTextureOperation opStencil;
    private BiTextureOperation opAdd;
    private MttTexturedQuad2D texturedQuad;
    private FreeCamera camera;

    private void resetBuffers() {
        bufScaleX[0] = bufScaleY[0] = bufScaleZ[0] = 1.0f;
    }

    @Override
    public void onInit(MttWindow initialWindow) {
        //Create screen for ImGui components
        imguiScreen = initialWindow.createScreen(new MttScreen.MttScreenCreateInfo());

        //Create main screen
        mainScreen = initialWindow.createScreen(new MttScreen.MttScreenCreateInfo());

        //Create final screen that will be presented to the window
        finalScreen = initialWindow.createScreen(new MttScreen.MttScreenCreateInfo());

        //Create ImGui instance for Mechtatel
        imgui = imguiScreen.createImGui();

        //Add flags to ImGui IO
        imgui.makeCurrent();
        ImGuiIO io = ImGui.getIO();
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);

        //Declare variables for ImGui inputs
        bufScaleX = new float[]{1.0f};
        bufScaleY = new float[]{1.0f};
        bufScaleZ = new float[]{1.0f};

        modelFilepathToLoad = "";

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
        }

        //Draw axes
        mainScreen.createLineSet().addPositiveAxes(10.0f).createBuffer();

        //Camera is set on the main screen
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

        //Draw ImGui components
        imguiScreen.draw();
        //Draw objects on main screen
        mainScreen.draw();

        //Create a stencil
        //Areas occupied by the objects drawn on the main screen are masked out as 0
        opStencil.run();

        //Add content of the main screen to the stencil
        opAdd.run();

        //Render to the final screen and present it
        finalScreen.draw();
        window.present(finalScreen);
    }

    private void createTextureOperations() {
        //Clean up texture operations if there is any
        if (opStencil != null) {
            opStencil.cleanup();
            opAdd.cleanup();
        }

        //Create stencil from ImGui and main screens
        //Mask out the areas covered with main objects from ImGui rendering
        MttTexture imguiColorTexture = imguiScreen.texturize(ScreenImageType.COLOR, finalScreen);
        MttTexture mainStencilTexture = mainScreen.texturize(ScreenImageType.STENCIL, finalScreen);

        opStencil = finalScreen.createBiTextureOperation(
                Arrays.asList(imguiColorTexture, mainStencilTexture),
                new ArrayList<>(),
                true
        );

        var texOpStencilParams = new BiTextureOperationParameters();
        texOpStencilParams.setOperationType(BiTextureOperationParameters.OperationType.MUL);
        opStencil.setBiParameters(texOpStencilParams);

        //Add rendering result of main screen to the stencil
        MttTexture stencilTexture = opStencil.getResultTexture();
        MttTexture mainColorTexture = mainScreen.texturize(ScreenImageType.COLOR, finalScreen);

        opAdd = finalScreen.createBiTextureOperation(
                Arrays.asList(stencilTexture, mainColorTexture),
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
