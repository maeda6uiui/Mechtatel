package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.physics.PhysicalObjects;
import com.github.maeda6uiui.mechtatel.core.screen.texture.MttTexture;
import com.github.maeda6uiui.mechtatel.core.vulkan.MttVulkanInstance;
import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderFactory;
import com.jme3.bullet.PhysicsSpace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Base class of the Mechtatel engine for headless mode
 *
 * @author maeda6uiui
 */
public class MechtatelHeadless implements IMechtatelHeadlessEventHandlers {
    private final Logger logger = LoggerFactory.getLogger(MechtatelHeadless.class);

    private boolean mechtatelReady;

    private int fps;
    private double secondsPerFrame;

    private MttHeadless initialInstance;
    private List<MttHeadless> instances;
    private List<MttHeadless> newInstancesQueue;

    private void initMechtatel(MttSettings settings, int width, int height) {
        //Initialize GLFW
        if (!glfwInit()) {
            throw new RuntimeException("Failed to initialize GLFW");
        }

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

        nativeLoader.loadLibbulletjme();
        PhysicalObjects.init(PhysicsSpace.BroadphaseType.DBVT);

        nativeLoader.loadShaderc();

        MttVulkanInstance.create(settings.vulkanSettings, false);
        MttTexture.setImageFormat(settings.renderingSettings.imageFormat);

        fps = settings.systemSettings.fps;
        secondsPerFrame = 1.0 / fps;

        instances = new ArrayList<>();
        newInstancesQueue = new ArrayList<>();

        initialInstance = new MttHeadless(this, settings, width, height);
        instances.add(initialInstance);

        this.onInit(initialInstance);
    }

    public MechtatelHeadless(MttSettings settings, int width, int height) {
        logger.debug(settings.toString());
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
            this.initMechtatel(settings, width, height);
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
            logger.error("Mechtatel engine is not ready to be run");
            return;
        }

        double lastTime = 0.0;
        glfwSetTime(0.0);

        logger.info("Starting the main loop...");

        //Continue until all instances are closed
        while (!instances.isEmpty()) {
            double currentTime = glfwGetTime();
            double elapsedTime = currentTime - lastTime;

            //Update instances if seconds per frame is elapsed
            if (elapsedTime >= secondsPerFrame) {
                //Clean up and remove instances that are flagged as closed
                Iterator<MttHeadless> it = instances.iterator();
                while (it.hasNext()) {
                    MttHeadless instance = it.next();
                    if (instance.shouldClose()) {
                        instance.cleanup();
                        it.remove();
                    }
                }

                //Update physics simulation
                PhysicalObjects.get().ifPresent(v -> v.updatePhysicsSpace((float) elapsedTime));

                //Call update handler for each instance
                instances.forEach(MttHeadless::update);

                //Add instance to queue if instance creation is requested
                if (!newInstancesQueue.isEmpty()) {
                    instances.addAll(newInstancesQueue);
                    newInstancesQueue.clear();
                }

                //Set current time for the next frame
                lastTime = glfwGetTime();
            }

            glfwPollEvents();
        }

        logger.info("Exiting the Mechtatel engine...");

        MttVulkanInstance.get().ifPresent(MttVulkanInstance::cleanup);

        this.onTerminate();
        glfwTerminate();
    }

    /**
     * Called after the Mechtatel engine is initialized and the initial instance is created.
     *
     * @param initialInstance Initial instance
     */
    public void onInit(MttHeadless initialInstance) {

    }

    /**
     * Called when the Mechtatel engine exits (right before the call to {@link org.lwjgl.glfw.GLFW#glfwTerminate()}).
     */
    public void onTerminate() {

    }

    @Override
    public void onCreate(MttHeadless instance) {

    }

    @Override
    public void onDispose(MttHeadless instance) {

    }

    @Override
    public void onUpdate(MttHeadless instance) {

    }

    public int getFPS() {
        return fps;
    }

    public double getSecondsPerFrame() {
        return secondsPerFrame;
    }
}
