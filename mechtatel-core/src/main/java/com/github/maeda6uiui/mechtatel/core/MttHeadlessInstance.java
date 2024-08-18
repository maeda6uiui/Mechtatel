package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;
import com.github.maeda6uiui.mechtatel.core.sound.MttSound;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanImplHeadless;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.internal.ImGuiContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides functionality of headless mode (rendering without a window)
 *
 * @author maeda6uiui
 */
public class MttHeadlessInstance {
    private static final Logger logger = LoggerFactory.getLogger(MttHeadlessInstance.class);

    private boolean shouldClose;
    private String instanceId;
    private IMechtatelHeadlessEventHandlers mtt;
    private MttVulkanImplHeadless vulkanImplHeadless;

    private ImGuiContext imguiContext;

    private MttScreen defaultScreen;
    private List<MttScreen> screens;

    private List<MttSound> sounds3D;

    public MttHeadlessInstance(IMechtatelHeadlessEventHandlers mtt, MttSettings settings) {
        this(
                mtt,
                settings,
                settings.headlessSettings.width,
                settings.headlessSettings.height
        );
    }

    public MttHeadlessInstance(IMechtatelHeadlessEventHandlers mtt, MttSettings settings, int width, int height) {
        shouldClose = false;
        instanceId = UUID.randomUUID().toString();
        this.mtt = mtt;
        vulkanImplHeadless = new MttVulkanImplHeadless(width, height);

        sounds3D = new ArrayList<>();

        //Set up ImGui =====
        imguiContext = ImGui.createContext();
        ImGui.setCurrentContext(imguiContext);

        ImGuiIO io = ImGui.getIO();

        io.setIniFilename(null);
        io.setDisplaySize(width, height);
        //==========

        defaultScreen = new MttScreen(
                vulkanImplHeadless,
                imguiContext,
                new MttScreen.MttScreenCreateInfo().setScreenWidth(width).setScreenHeight(height)
        );
        screens = new ArrayList<>();
        screens.add(defaultScreen);

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
        sounds3D.forEach(MttSound::cleanup);

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

    public MttSound createSound(URL soundResource, boolean loop, boolean relative) throws URISyntaxException, IOException {
        var sound = new MttSound(soundResource.toURI(), loop, relative);
        sounds3D.add(sound);

        return sound;
    }

    public MttSound duplicateSound(MttSound srcSound, boolean loop, boolean relative) {
        var sound = new MttSound(srcSound, loop, relative);
        sounds3D.add(sound);

        return sound;
    }

    public boolean deleteSound(MttSound sound) {
        if (sounds3D.contains(sound)) {
            sound.cleanup();
            sounds3D.remove(sound);

            return true;
        }

        return false;
    }
}
