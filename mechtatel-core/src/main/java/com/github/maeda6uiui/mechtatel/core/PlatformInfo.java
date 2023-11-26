package com.github.maeda6uiui.mechtatel.core;

import org.lwjgl.system.Platform;

/**
 * Platform info
 *
 * @author maeda6uiui
 */
public class PlatformInfo {
    public static final String PLATFORM;
    public static final String ARCHITECTURE;

    static {
        PLATFORM = Platform.get().name().toLowerCase();
        ARCHITECTURE = Platform.getArchitecture().name().toLowerCase();
    }
}
