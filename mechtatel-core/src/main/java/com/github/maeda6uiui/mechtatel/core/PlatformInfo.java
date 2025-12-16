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
    public static final String PLATFORM_WITH_ARCH;

    static {
        PLATFORM = Platform.get().name().toLowerCase();
        ARCHITECTURE = Platform.getArchitecture().name().toLowerCase();
        
        if (PLATFORM.equals("linux")) {
            if (ARCHITECTURE.equals("x64")) {
                PLATFORM_WITH_ARCH = PLATFORM;
            } else if (ARCHITECTURE.equals("arm64")) {
                PLATFORM_WITH_ARCH = PLATFORM + "arm64";
            } else {
                throw new RuntimeException("Unsupported architecture: " + ARCHITECTURE);
            }
        } else {
            PLATFORM_WITH_ARCH = PLATFORM;
        }
    }
}
