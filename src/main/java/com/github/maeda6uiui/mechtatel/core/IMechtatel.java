package com.github.maeda6uiui.mechtatel.core;

/**
 * Provides an interface of Mechtatel to the subordinate classes
 *
 * @author maeda
 */
interface IMechtatel {
    public void init();

    public void dispose();

    public void reshape(int width, int height);

    public void update();

    public void draw();
}
