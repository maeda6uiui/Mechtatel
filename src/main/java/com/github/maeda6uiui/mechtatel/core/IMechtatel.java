package com.github.maeda6uiui.mechtatel.core;

/**
 * Provides an interface of Mechtatel to the subordinate classes
 *
 * @author maeda6uiui
 */
interface IMechtatel {
    public void init();

    public void dispose();

    public void reshape(int width, int height);

    public void update();
}
