package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.nabor.FlexibleNaborInfo;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private String samplerFilter;
    private String samplerMipmapMode;
    private String samplerAddressMode;
    private boolean shouldChangeExtentOnRecreate;
    private boolean useShadowMapping;
    private Map<String, FlexibleNaborInfo> flexibleNaborInfos;
    private List<String> ppNaborNames;

    public ScreenCreator(IMechtatelForScreenCreator mtt, String screenName) {
        this.mtt = mtt;

        this.screenName = screenName;
        depthImageWidth = 2048;
        depthImageHeight = 2048;
        screenWidth = -1;
        screenHeight = -1;
        samplerFilter = "nearest";
        samplerMipmapMode = "nearest";
        samplerAddressMode = "repeat";
        shouldChangeExtentOnRecreate = true;
        useShadowMapping = false;
        flexibleNaborInfos = new HashMap<>();
        ppNaborNames = new ArrayList<>();
    }

    public void reset() {
        depthImageWidth = 2048;
        depthImageHeight = 2048;
        screenWidth = -1;
        screenHeight = -1;
        samplerFilter = "nearest";
        samplerMipmapMode = "nearest";
        samplerAddressMode = "repeat";
        shouldChangeExtentOnRecreate = true;
        useShadowMapping = false;
        flexibleNaborInfos.clear();
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

    public void setSamplerFilter(String samplerFilter) {
        this.samplerFilter = samplerFilter;
    }

    public void setSamplerMipmapMode(String samplerMipmapMode) {
        this.samplerMipmapMode = samplerMipmapMode;
    }

    public void setSamplerAddressMode(String samplerAddressMode) {
        this.samplerAddressMode = samplerAddressMode;
    }

    public void setShouldChangeExtentOnRecreate(boolean shouldChangeExtentOnRecreate) {
        this.shouldChangeExtentOnRecreate = shouldChangeExtentOnRecreate;
    }

    public void setUseShadowMapping(boolean useShadowMapping) {
        this.useShadowMapping = useShadowMapping;
    }

    public void clearFlexibleNaborInfos() {
        flexibleNaborInfos.clear();
    }

    public void addFlexibleNaborInfo(String naborName, FlexibleNaborInfo flexibleNaborInfo) {
        flexibleNaborInfos.put(naborName, flexibleNaborInfo);
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
                samplerFilter,
                samplerMipmapMode,
                samplerAddressMode,
                shouldChangeExtentOnRecreate,
                useShadowMapping,
                flexibleNaborInfos,
                ppNaborNames
        );
        return screen;
    }
}