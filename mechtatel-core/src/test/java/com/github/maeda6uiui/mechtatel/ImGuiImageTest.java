package com.github.maeda6uiui.mechtatel;

import com.github.maeda6uiui.mechtatel.core.Mechtatel;
import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.MttWindow;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttImGui;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import imgui.ImGui;
import org.joml.Vector4f;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImGuiImageTest extends Mechtatel {
    private static final Logger logger = LoggerFactory.getLogger(ImGuiImageTest.class);

    public ImGuiImageTest(MttSettings settings) {
        super(settings);
        this.run();
    }

    public static void main(String[] args) {
        MttSettings
                .load("./Mechtatel/settings.json")
                .ifPresentOrElse(
                        ImGuiImageTest::new,
                        () -> logger.error("Failed to load settings")
                );
    }

    private MttScreen mainScreen;
    private MttScreen imguiScreen;

    private MttImGui imgui;
    private MttTexture texture;

    @Override
    public void onInit(MttWindow initialWindow) {
        mainScreen = initialWindow.createScreen(
                new MttScreen.MttScreenCreateInfo()
        );
        mainScreen.createLineSet().addAxes(10.0f).createBuffer();
        mainScreen.createSphere(1.0f, 16, 16, new Vector4f(1.0f), false);

        imguiScreen = initialWindow.createScreen(
                new MttScreen.MttScreenCreateInfo()
        );
        imgui = imguiScreen.createImGui();

        texture = mainScreen.texturize(ScreenImageType.COLOR, imguiScreen);
    }

    @Override
    public void onUpdate(MttWindow window) {
        imgui.declare(() -> {
            ImGui.image(texture.getVulkanTexture().getAllocationIndex(), 640, 480);
        });

        mainScreen.draw();
        imguiScreen.draw();
        window.present(imguiScreen);
    }
}
