package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.physics.MttDefaultPhysicsSpace;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderFactory;
import com.jme3.bullet.PhysicsSpace;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.openal.ALC10.*;

/**
 * Base class of the Mechtatel engine
 *
 * @author maeda6uiui
 */
public class Mechtatel implements IMechtatelWindowEventHandlers {
    private final Logger logger = LoggerFactory.getLogger(Mechtatel.class);

    private boolean mechtatelReady;

    private double secondsPerFrame;
    private double actualSecondsPerFrame;

    private MttWindow initialWindow;
    private List<MttWindow> windows;
    private List<MttWindow> newWindowsQueue;

    private void initMechtatel(MttSettings settings) {
        //Initialize GLFW =====
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

        glfwWindowHint(GLFW_CLIENT_API, GLFW_NO_API);
        if (settings.windowSettings.resizable) {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);
        } else {
            glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE);
        }
        //==========

        //Set up OpenAL =====
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
        //==========

        //Load native libraries
        IMttNativeLoader nativeLoader;
        try {
            nativeLoader = MttNativeLoaderFactory.createNativeLoader(PlatformInfo.PLATFORM);
        } catch (ClassNotFoundException
                 | NoSuchMethodException
                 | InstantiationException
                 | IllegalAccessException
                 | InvocationTargetException e) {
            logger.error("Failed to create native loader", e);
            throw new RuntimeException(e);
        }

        try {
            nativeLoader.loadLibbulletjme();
            nativeLoader.loadShaderc();
        } catch (IOException e) {
            logger.error("Failed to load native library");
            throw new RuntimeException(e);
        }
        //==========

        MttDefaultPhysicsSpace.init(PhysicsSpace.BroadphaseType.DBVT);

        MttVulkanInstance.create(settings.vulkanSettings, false);
        MttTexture.setImageFormat(settings.renderingSettings.imageFormat);

        int fps = settings.systemSettings.fps;
        secondsPerFrame = 1.0 / fps;

        windows = new ArrayList<>();
        newWindowsQueue = new ArrayList<>();

        initialWindow = new MttWindow(this, settings);
        windows.add(initialWindow);

        this.onInit(initialWindow);
    }

    public Mechtatel(MttSettings settings) {
        logger.debug("Platform={} Architecture={}", PlatformInfo.PLATFORM, PlatformInfo.ARCHITECTURE);
        logger.debug(
                "Mechtatel version={}.{}.{}",
                EngineInfo.MAJOR_VERSION,
                EngineInfo.MINOR_VERSION,
                EngineInfo.PATCH_VERSION
        );
        logger.info("Starting the Mechtatel engine...");

        mechtatelReady = true;
        try {
            this.initMechtatel(settings);
        } catch (RuntimeException e) {
            logger.error("Fatal error", e);

            glfwTerminate();
            mechtatelReady = false;

            return;
        }

        logger.info("Mechtatel engine successfully started");
    }

    public void run() {
        if (!mechtatelReady) {
            logger.error("Mechtatel engine is not ready");
            return;
        }

        double lastTime = 0.0;
        glfwSetTime(0.0);

        logger.info("Starting the main loop...");

        //Continue until all windows are closed
        while (!windows.isEmpty()) {
            double currentTime = glfwGetTime();
            double elapsedTime = currentTime - lastTime;

            //Update windows if seconds per frame is elapsed
            if (elapsedTime >= secondsPerFrame) {
                actualSecondsPerFrame = elapsedTime;

                //Clean up and remove windows that are flagged as closed
                Iterator<MttWindow> it = windows.iterator();
                while (it.hasNext()) {
                    MttWindow window = it.next();
                    if (window.shouldClose()) {
                        window.cleanup();
                        it.remove();
                    }
                }

                //Update physics simulation
                MttDefaultPhysicsSpace.get().ifPresent(v -> v.updatePhysicsSpace((float) elapsedTime));

                //Call update handler for each window
                windows.forEach(MttWindow::update);

                //Add window to queue if window creation is requested
                if (!newWindowsQueue.isEmpty()) {
                    windows.addAll(newWindowsQueue);
                    newWindowsQueue.clear();
                }

                //Set current time for the next frame
                lastTime = currentTime;
            }

            glfwPollEvents();
        }

        logger.info("Exiting the Mechtatel engine...");

        MttVulkanInstance.get().ifPresent(MttVulkanInstance::cleanup);

        MttWindow.destroyImGuiContexts();

        glfwTerminate();
        this.onTerminate();
    }

    public MttWindow getInitialWindow() {
        return initialWindow;
    }

    public void registerWindow(MttWindow window) {
        newWindowsQueue.add(window);
    }

    public void closeAllWindows() {
        windows.forEach(MttWindow::close);
    }

    /**
     * Called after the Mechtatel engine is initialized and the initial window is created.
     *
     * @param initialWindow Initial window
     */
    public void onInit(MttWindow initialWindow) {

    }

    /**
     * Called when the Mechtatel engine exits (right after the call to {@link org.lwjgl.glfw.GLFW#glfwTerminate()}).
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

    public double getSecondsPerFrame() {
        return secondsPerFrame;
    }

    public double getActualSecondsPerFrame() {
        return actualSecondsPerFrame;
    }
}
