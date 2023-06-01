package com.github.maeda6uiui.mechtatel.core;

import com.github.maeda6uiui.mechtatel.core.screen.ExtraPostProcessingNaborInfo;
import com.github.maeda6uiui.mechtatel.core.screen.MttScreen;

import java.util.List;
import java.util.Map;

/**
 * Interface to Mechtatel providing access to methods relating to screen creation
 *
 * @author maeda6uiui
 */
public interface IMechtatelForScreenCreator {
    MttScreen createScreen(
            String screenName,
            int depthImageWidth,
            int depthImageHeight,
            int screenWidth,
            int screenHeight,
            boolean shouldChangeExtentOnRecreate,
            String samplerFilter,
            String samplerMipmapMode,
            String samplerAddressMode,
            Map<String, ExtraPostProcessingNaborInfo> extraPPNaborInfos,
            List<String> ppNaborNames);
}
