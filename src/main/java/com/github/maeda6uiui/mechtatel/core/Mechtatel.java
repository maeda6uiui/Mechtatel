package com.github.maeda6uiui.mechtatel.core;

/**
 * Base class of the Mechtatel engine
 *
 * @author maeda
 */
public class Mechtatel implements IMechtatel {
    private MttInstance instance;

    public Mechtatel() {
        instance = new MttInstance(this, 1280, 720, "Mechtatel", true);
        instance.run();
        instance.cleanup();
    }

    @Override
    public void init() {

    }

    @Override
    public void dispose() {

    }

    @Override
    public void reshape(int width, int height) {

    }

    @Override
    public void update() {

    }

    @Override
    public void draw() {

    }
}
