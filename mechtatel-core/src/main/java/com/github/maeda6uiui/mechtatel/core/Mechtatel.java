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

        this.onInit();
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

                windows.forEach(MttWindow::update);
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

        this.onTerminate();
        glfwTerminate();
    }

    public MttWindow getInitialWindow() {
        return initialWindow;
    }

    public void registerWindow(MttWindow window) {
        newWindowsQueue.add(window);
    }

    /**
     * Called after the Mechtatel engine is initialized and the initial window is created.
     */
    public void onInit() {

    }

    /**
     * Called when the Mechtatel engine exits (right before the call to {@link org.lwjgl.glfw.GLFW#glfwTerminate()}).
     */
    public void onTerminate() {

    }

    @Override
    public void onCreate(MttWindow window) {

    }

    @Override
    public void onDispose(MttWindow window) {

    }

    @Override
    public void onReshape(MttWindow window, int width, int height) {

    }

    @Override
    public void onUpdate(MttWindow window) {

    }

    @Override
    public void onRecreate(MttWindow window, int width, int height) {

    }

    public int getFPS() {
        return fps;
    }

    public float getSecondsPerFrame() {
        return secondsPerFrame;
    }
}
