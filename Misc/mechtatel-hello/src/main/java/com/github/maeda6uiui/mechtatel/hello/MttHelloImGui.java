package com.github.maeda6uiui.mechtatel.hello;

import com.github.maeda6uiui.mechtatel.core.MttHeadlessInstance;
import com.github.maeda6uiui.mechtatel.core.PixelFormat;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.screen.ScreenImageType;
import com.github.maeda6uiui.mechtatel.core.screen.component.MttImGui;
import imgui.ImGui;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;

/**
 * Rendering with ImGui
 *
 * @author maeda6uiui
 */
class MttHelloImGui implements IMttHello {
    private static final Logger logger = LoggerFactory.getLogger(MttHelloImGui.class);

    private MttImGui imgui;

    @Override
    public void onInit(MttHeadlessInstance instance) {
        MttScreen defaultScreen = instance.getDefaultScreen();
        imgui = defaultScreen.createImGui();
    }

    @Override
    public void onUpdate(MttHeadlessInstance instance, Path imageOutputPath) {
        imgui.declare(ImGui::showDemoWindow);

        MttScreen defaultScreen = instance.getDefaultScreen();
        defaultScreen.draw();

        try {
            defaultScreen.save(ScreenImageType.COLOR, PixelFormat.BGRA, imageOutputPath);
        } catch (IOException e) {
            logger.error("Error while saving image to disk", e);
        } finally {
            instance.close();
        }
    }
}
