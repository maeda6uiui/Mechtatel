package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.camera.Camera;
import com.github.maeda6uiui.mechtatel.core.component.Model3D;
import com.github.maeda6uiui.mechtatel.core.light.ParallelLight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base class of the Mechtatel engine
 *
 * @author maeda
 */
public class Mechtatel implements IMechtatel {
    private final Logger logger = LoggerFactory.getLogger(Mechtatel.class);

    private MttInstance instance;

    public Mechtatel(MttSettings settings) {
        logger.info(settings.toString());

        try {
            instance = new MttInstance(this, settings);
            logger.info("MttInstance successfully created");

            logger.info("Start running the main loop");
            instance.run();

            logger.info("Start cleaning up Mechtatel");
            instance.cleanup();
        } catch (Exception e) {
            logger.error("Fatal error", e);
            return;
        }
    }

    @Override
    public void init() {
        logger.info("Mechtatel initialized");
    }

    @Override
    public void dispose() {
        logger.info("Mechtatel disposed");
    }

    @Override
    public void reshape(int width, int height) {
        logger.trace("Framebuffer size changed: (width, height)=({},{})", width, height);
    }

    @Override
    public void update() {

    }

    public Camera getCamera() {
        return instance.getCamera();
    }

    public ParallelLight getParallelLight() {
        return instance.getParallelLight();
    }

    //=== Methods relating to components ===
    public Model3D createModel3D(String modelFilepath) {
        return instance.createModel3D(modelFilepath);
    }
}
