package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.physics.PhysicalObjects;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.jme3.bullet.PhysicsSpace;
import com.jme3.system.NativeLibraryLoader;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;

/**
 * Base class of the Mechtatel engine
 *
 * @author maeda6uiui
 */
public class Mechtatel implements IMechtatelForMttWindow {
    private final Logger logger = LoggerFactory.getLogger(Mechtatel.class);

    private int fps;
    private float secondsPerFrame;

    private MttWindow initialWindow;
    private List<MttWindow> windows;
    private List<MttWindow> newWindowsQueue;

    private void initMechtatel(MttSettings settings) {
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        if (settings.windowSettings.resizable) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        } else {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        }

        long alcDevice = alcOpenDevice((ByteBuffer) null);
        if (alcDevice == 0) {
            throw new RuntimeException("Failed to open default OpenAL device");
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(alcDevice);

        long alcContext = alcCreateContext(alcDevice, (IntBuffer) null);
        if (alcContext == 0) {
            throw new RuntimeException("Failed to create OpenAL context");
        }

        alcMakeContextCurrent(alcContext);
        AL.createCapabilities(deviceCaps);

        NativeLibraryLoader.loadLibbulletjme(
                true,
                new File(Objects.requireNonNull(this.getClass().getResource("/Bin")).getFile()),
                "Release",
                "Sp"
        );

        if (settings.renderingSettings.engine == RenderingEngine.VULKAN) {
            String shadercLibFilename;
            switch (PlatformInfo.PLATFORM) {
                case "windows" -> shadercLibFilename = "shaderc_shared.dll";
                case "linux" -> shadercLibFilename = "libshaderc_shared.so";
                case "macos" -> shadercLibFilename = "libshaderc_shared.dylib";
                default -> throw new IllegalArgumentException(
                        "shaderc library is not available for this platform: " + PlatformInfo.PLATFORM);
            }

            String shadercLibFilepath = Objects.requireNonNull(
                    this.getClass().getResource("/Bin/" + shadercLibFilename)).getFile();
            System.load(shadercLibFilepath);

            MttVulkanInstance.create(settings.vulkanSettings, false);
        }

        MttTexture.setImageFormat(settings.renderingSettings.imageFormat);

        PhysicalObjects.init(PhysicsSpace.BroadphaseType.DBVT);

        fps = settings.systemSettings.fps;
        secondsPerFrame = 1.0f / fps;

        windows = new ArrayList<>();
        newWindowsQueue = new ArrayList<>();

        initialWindow = new MttWindow(this, settings);
        windows.add(initialWindow);
    }

    public Mechtatel(MttSettings settings) {
        logger.debug(settings.toString());
        logger.debug("Platform={} Architecture={}", PlatformInfo.PLATFORM, PlatformInfo.ARCHITECTURE);
        logger.debug(
                "Mechtatel version={}.{}.{}",
                EngineInfo.MAJOR_VERSION,
                EngineInfo.MINOR_VERSION,
                EngineInfo.PATCH_VERSION
        );
        logger.info("Starting the Mechtatel engine...");

        try {
            this.initMechtatel(settings);
        } catch (RuntimeException e) {
            logger.error("Fatal error", e);
            glfwTerminate();

            return;
        }

        logger.info("Mechtatel engine successfully started");
    }

    public void run() {
        double lastTime = 0.0;
        glfwSetTime(0.0);

        logger.info("Starting the main loop...");

        while (!windows.isEmpty()) {
            double currentTime = glfwGetTime();
            double elapsedTime = currentTime - lastTime;
            if (elapsedTime >= secondsPerFrame) {
                Iterator<MttWindow> it = windows.iterator();
                while (it.hasNext()) {
                    MttWindow window = it.next();
                    if (window.shouldClose()) {
                        window.cleanup();
                        it.remove();
                    }
                }

                PhysicalObjects.get().ifPresent(v -> v.updatePhysicsSpace((float) elapsedTime));

                windows.forEach(window -> {
                    window.update();
                    window.draw();
                });
                if (!newWindowsQueue.isEmpty()) {
                    windows.addAll(newWindowsQueue);
                    newWindowsQueue.clear();
                }

                lastTime = glfwGetTime();
            }

            glfwPollEvents();
        }

        logger.info("Exiting the Mechtatel engine...");

        MttVulkanInstance.get().ifPresent(MttVulkanInstance::cleanup);
        glfwTerminate();
    }

    public MttWindow getInitialWindow() {
        return initialWindow;
    }

    public void registerWindow(MttWindow window) {
        newWindowsQueue.add(window);
    }

    @Override
    public void init(MttWindow window) {

    }

    @Override
    public void dispose(MttWindow window) {

    }

    @Override
    public void reshape(MttWindow window, int width, int height) {

    }

    @Override
    public void update(MttWindow window) {

    }

    @Override
    public void preDraw(MttWindow window, String screenName) {

    }

    @Override
    public void postDraw(MttWindow window, String screenName) {

    }

    @Override
    public void preTextureOperation(MttWindow window, String operationName) {

    }

    @Override
    public void postTextureOperation(MttWindow window, String operationName) {

    }

    @Override
    public void preDeferredDraw(MttWindow window, String screenName) {

    }

    @Override
    public void postDeferredDraw(MttWindow window, String screenName) {

    }

    @Override
    public void prePresent(MttWindow window) {

    }

    @Override
    public void postPresent(MttWindow window) {

    }

    public int getFPS() {
        return fps;
    }

    public float getSecondsPerFrame() {
        return secondsPerFrame;
    }
}
