package com.github.maeda6uiui.mechtatel.core;

/**
 * Provides an interface to event handlers of window
 *
 * @author maeda6uiui
 */
interface IMechtatelWindowEventHandlers {
    /**
     * Called after the window is successfully created.
     *
     * @param window Window
     */
    void onCreate(MttWindow window);

    /**
     * Called before the clean-up process of the window begins.
     *
     * @param window Window
     */
    void onDispose(MttWindow window);

    /**
     * Called every time the window is resized.
     * The purpose of this method is to tell the user that the window is resized,
     * and it is not in sync with the underlying resource recreations.
     * Use {@link #onRecreate(MttWindow, int, int)} if you want to run some procedure
     * <i>after</i> underlying resources are successfully recreated.
     *
     * @param window Window
     * @param width  New width
     * @param height New height
     */
    void onReshape(MttWindow window, int width, int height);

    /**
     * Called in each frame after update of resources belonging to the window is completed.
     *
     * @param window Window
     */
    void onUpdate(MttWindow window);

    /**
     * Called after the resource recreations caused by window resize are successfully completed.
     * Resource recreations take place in the first frame after the window is resized,
     * therefore the interval of the call to this method is subject to the frames per second of the Mechtatel engine.
     * Use {@link #onReshape(MttWindow, int, int)} if you want to be notified of window resize in realtime.
     *
     * @param window Window
     * @param width  New width
     * @param height New height
     */
    void onRecreate(MttWindow window, int width, int height);
}
