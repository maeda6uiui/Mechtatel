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

    void preDraw(MttWindow window, String screenName);

    void postDraw(MttWindow window, String screenName);

    void preTextureOperation(MttWindow window, String operationName);

    void postTextureOperation(MttWindow window, String operationName);

    void preDeferredDraw(MttWindow window, String screenName);

    void postDeferredDraw(MttWindow window, String screenName);

    void prePresent(MttWindow window);

    void postPresent(MttWindow window);
}
