package com.github.maeda6uiui.mechtatel.core;

/**
 * Provides an interface of Mechtatel to the subordinate classes
 *
 * @author maeda6uiui
 */
interface IMechtatel {
    void init();

    void dispose();

    void reshape(int width, int height);

    void update();
}
