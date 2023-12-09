package com.github.maeda6uiui.mechtatel.core;

/**
 * Provides an interface of {@link Mechtatel} to {@link MttWindow}
 *
 * @author maeda6uiui
 */
interface IMechtatelForMttWindow {
    void init(MttWindow window);

    void dispose(MttWindow window);

    void reshape(MttWindow window, int width, int height);

    void update(MttWindow window);
}
