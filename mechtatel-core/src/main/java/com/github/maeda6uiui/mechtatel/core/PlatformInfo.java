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
        PLATFORM_WITH_ARCH = resolvePlatformWithArch(PLATFORM, ARCHITECTURE);
    }

    static String resolvePlatformWithArch(String platform, String architecture) {
        switch (platform) {
            case "linux", "windows" -> {
                if (architecture.equals("x64")) {
                    return platform;
                } else if (architecture.equals("arm64")) {
                    return platform + "arm64";
                } else {
                    throw new RuntimeException("Unsupported architecture: " + architecture);
                }
            }
            case "macos" -> {
                if (architecture.equals("arm64")) {
                    return platform + "arm64";
                } else {
                    throw new RuntimeException("Unsupported architecture: " + architecture);
                }
            }
            default -> {
                return platform;
            }
        }
    }
}
