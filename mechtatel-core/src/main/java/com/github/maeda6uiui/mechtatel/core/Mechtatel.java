package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.texture.MttTexture;
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
    private List<MttWindow> windows;
    private List<MttWindow> newWindowsQueue;

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

        if (!glfwInit()) {
            logger.error("Failed to initialize GLFW");
            return;
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        if (settings.windowSettings.resizable) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        } else {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        }

        long alcDevice = alcOpenDevice((ByteBuffer) null);
        if (alcDevice == 0) {
            logger.error("Failed to open default OpenAL device");
            glfwTerminate();

            return;
        }
        ALCCapabilities deviceCaps = ALC.createCapabilities(alcDevice);

        long alcContext = alcCreateContext(alcDevice, (IntBuffer) null);
        if (alcContext == 0) {
            logger.error("Failed to create OpenAL context");
            glfwTerminate();

            return;
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
                default -> {
                    logger.error("shaderc library is not available for this platform: {}", PlatformInfo.PLATFORM);
                    glfwTerminate();

                    return;
                }
            }

            String shadercLibFilepath = Objects.requireNonNull(
                    this.getClass().getResource("/Bin/" + shadercLibFilename)).getFile();
            System.load(shadercLibFilepath);
        }

        MttTexture.setImageFormat(settings.renderingSettings.imageFormat);

        fps = settings.systemSettings.fps;
        secondsPerFrame = 1.0f / fps;

        windows = new ArrayList<>();
        newWindowsQueue = new ArrayList<>();
        try {
            //Create a primary window
            var window = new MttWindow(this, settings);
            windows.add(window);
            logger.info("Primary window successfully created");

            //Start the main loop
            logger.info("Start the main loop...");
            this.run();
        } catch (Exception e) {
            logger.error("Fatal error", e);
        } finally {
            logger.info("Exiting the Mechtatel engine...");
            glfwTerminate();
        }
    }

    public void run() {
        double lastTime = 0.0;
        glfwSetTime(0.0);

        while (!windows.isEmpty()) {
            double currentTime = glfwGetTime();
            double elapsedTime = currentTime - lastTime;

            Iterator<MttWindow> it = windows.iterator();
            while (it.hasNext()) {
                MttWindow window = it.next();
                if (window.shouldClose()) {
                    window.cleanup();
                    it.remove();
                }
            }

            windows.forEach(window -> {
                window.update(elapsedTime);
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

    @Override
    public void registerWindow(MttWindow window) {
        newWindowsQueue.add(window);
    }

    @Override
    public void init(MttWindow window) {
        logger.info("Mechtatel initialized");
    }

    @Override
    public void dispose(MttWindow window) {
        logger.info("Mechtatel disposed");
    }

    @Override
    public void reshape(MttWindow window, int width, int height) {
        logger.trace("Framebuffer size changed: (width, height)=({},{})", width, height);
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
