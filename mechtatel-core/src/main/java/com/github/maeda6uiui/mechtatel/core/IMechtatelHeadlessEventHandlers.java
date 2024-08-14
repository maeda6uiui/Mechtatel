package com.github.maeda6uiui.mechtatel.core;

/**
 * Provides an interface to event handlers of headless instance
 *
 * @author maeda6uiui
 */
public interface IMechtatelHeadlessEventHandlers {
    /**
     * Called after the headless instance is successfully created.
     *
     * @param headless Headless instance
     */
    void onCreate(MttHeadlessInstance headless);

    /**
     * Called before the clean-up process of the headless instance begins.
     *
     * @param headless Headless instance
     */
    void onDispose(MttHeadlessInstance headless);

    /**
     * Called in each frame after update of resources belonging to the headless instance is completed.
     *
     * @param headless Headless instance
     */
    void onUpdate(MttHeadlessInstance headless);
}
