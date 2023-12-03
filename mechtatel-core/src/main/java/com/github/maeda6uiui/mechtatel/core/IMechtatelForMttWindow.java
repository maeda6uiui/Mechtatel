package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;

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

    void preDraw(MttWindow window, MttScreen screen);

    void postDraw(MttWindow window, MttScreen screen);

    void preTextureOperation(MttWindow window, TextureOperation textureOperation);

    void postTextureOperation(MttWindow window, TextureOperation textureOperation);

    void preDeferredDraw(MttWindow window, MttScreen screen);

    void postDeferredDraw(MttWindow window, MttScreen screen);

    void prePresent(MttWindow window);

    void postPresent(MttWindow window);
}
