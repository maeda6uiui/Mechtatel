package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class to create a screen
 *
 * @author maeda6uiui
 */
public class ScreenCreator {
    private IMechtatelForScreenCreator mtt;

    private String screenName;
    private int depthImageWidth;
    private int depthImageHeight;
    private int screenWidth;
    private int screenHeight;
    private boolean shouldChangeExtentOnRecreate;
    private List<String> ppNaborNames;

    public ScreenCreator(IMechtatelForScreenCreator mtt, String screenName) {
        this.mtt = mtt;

        this.screenName = screenName;
        depthImageWidth = 2048;
        depthImageHeight = 2048;
        screenWidth = -1;
        screenHeight = -1;
        shouldChangeExtentOnRecreate = true;
        ppNaborNames = new ArrayList<>();
    }

    public void reset() {
        depthImageWidth = 2048;
        depthImageHeight = 2048;
        screenWidth = -1;
        screenHeight = -1;
        shouldChangeExtentOnRecreate = true;
        ppNaborNames.clear();
    }

    public void setDepthImageSize(int width, int height) {
        depthImageWidth = width;
        depthImageHeight = height;
    }

    public void setScreenSize(int width, int height) {
        screenWidth = width;
        screenHeight = height;
    }

    public void setShouldChangeExtentOnRecreate(boolean shouldChangeExtentOnRecreate) {
        this.shouldChangeExtentOnRecreate = shouldChangeExtentOnRecreate;
    }

    public void clearPostProcessingNabors() {
        ppNaborNames.clear();
    }

    public void addPostProcessingNabor(String naborName) {
        ppNaborNames.add(naborName);
    }

    public MttScreen create() {
        MttScreen screen = mtt.createScreen(
                screenName,
                depthImageWidth,
                depthImageHeight,
                screenWidth,
                screenHeight,
                shouldChangeExtentOnRecreate,
                ppNaborNames
        );
        return screen;
    }
}