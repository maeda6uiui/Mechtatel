package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImplHeadless;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.internal.ImGuiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides functionality of headless mode (rendering without a window)
 *
 * @author maeda6uiui
 */
public class MttHeadless {
    private static final Logger logger = LoggerFactory.getLogger(MttHeadless.class);

    private boolean shouldClose;

    private String instanceId;

    private IMechtatelHeadlessEventHandlers mtt;
    private MttVulkanImplHeadless vulkanImplHeadless;

    private ImGuiContext imguiContext;

    private MttScreen defaultScreen;
    private List<MttScreen> screens;

    public MttHeadless(IMechtatelHeadlessEventHandlers mtt, MttSettings settings, int width, int height) {
        //Set should close flag
        shouldClose = false;

        //Get unique ID for this instance
        instanceId = UUID.randomUUID().toString();

        //Set interface of Mechtatel
        this.mtt = mtt;

        //Set up Vulkan implementation
        vulkanImplHeadless = new MttVulkanImplHeadless(settings.vulkanSettings, width, height);

        //Set up ImGui =====
        //Create context and make it current
        imguiContext = ImGui.createContext();
        ImGui.setCurrentContext(imguiContext);

        //Get IO
        ImGuiIO io = ImGui.getIO();

        //Not create ini file
        io.setIniFilename(null);
        //Set screen size
        io.setDisplaySize(width, height);
        //==========

        //Create default screen
        defaultScreen = new MttScreen(
                vulkanImplHeadless,
                imguiContext,
                new MttScreen.MttScreenCreateInfo().setScreenWidth(width).setScreenHeight(height)
        );
        screens = new ArrayList<>();
        screens.add(defaultScreen);

        //Call onCreate handler
        mtt.onCreate(this);
    }

    public void update() {
        screens.forEach(screen -> {
            screen.removeGarbageComponents();
            screen.removeGarbageTextures();
            screen.removeGarbageTextureOperations();
        });

        mtt.onUpdate(this);
    }

    public void cleanup() {
        mtt.onDispose(this);

        screens.forEach(MttScreen::cleanup);
        vulkanImplHeadless.cleanup();

        ImGui.destroyContext(imguiContext);
    }

    public Optional<MttVulkanImplHeadless> getVulkanImplHeadless() {
        return Optional.ofNullable(vulkanImplHeadless);
    }

    public void close() {
        shouldClose = true;
    }

    public boolean shouldClose() {
        return shouldClose;
    }

    public String getInstanceId() {
        return instanceId;
    }

    public MttScreen getDefaultScreen() {
        return defaultScreen;
    }

    public MttScreen createScreen(MttScreen.MttScreenCreateInfo createInfo) {
        var screen = new MttScreen(vulkanImplHeadless, imguiContext, createInfo);
        screens.add(screen);

        return screen;
    }

    public boolean deleteScreen(MttScreen screen) {
        if (screen == defaultScreen) {
            logger.warn("You cannot delete default screen");
            return false;
        }

        screen.cleanup();
        return screens.remove(screen);
    }

    public void deleteAllScreens() {
        screens
                .stream()
                .filter(screen -> screen != defaultScreen)
                .forEach(MttScreen::cleanup);
        screens.clear();
        screens.add(defaultScreen);
    }
}
