package com.github.maeda6uiui.mechtatel.core;

/**
 * Provides an interface of Mechtatel to the subordinate classes
 *
 * @author maeda6uiui
 */
interface IMechtatelForMttInstance {
    void init();

    void dispose();

    void reshape(int width, int height);

    void update();

    void preDraw(String screenName);

    void postDraw(String screenName);

    void preTextureOperation(String operationName);

    void postTextureOperation(String operationName);

    void preDeferredDraw(String screenName);

    void postDeferredDraw(String screenName);

    void prePresent();

    void postPresent();
}
