package com.github.maeda6uiui.mechtatel.core;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PlatformInfoTest {
    @ParameterizedTest
    @CsvSource({
            "linux,x64,linux",
            "linux,arm64,linuxarm64",
            "windows,x64,windows",
            "windows,arm64,windowsarm64",
            "macos,arm64,macosarm64"
    })
    void resolvesSupportedCombinations(String platform, String arch, String expected) {
        assertEquals(expected, PlatformInfo.resolvePlatformWithArch(platform, arch));
    }

    @ParameterizedTest
    @CsvSource({
            "linux,x86",
            "linux,riscv64",
            "windows,x86",
            "macos,x64"
    })
    void rejectsUnsupportedArchitectures(String platform, String arch) {
        assertThrows(RuntimeException.class, () -> PlatformInfo.resolvePlatformWithArch(platform, arch));
    }

    @Test
    void unknownPlatformIsReturnedAsIs() {
        assertEquals("unknown", PlatformInfo.resolvePlatformWithArch("unknown", "x64"));
    }

    @Test
    void currentPlatformIsConsistent() {
        assertEquals(
                PlatformInfo.resolvePlatformWithArch(PlatformInfo.PLATFORM, PlatformInfo.ARCHITECTURE),
                PlatformInfo.PLATFORM_WITH_ARCH);
    }
}
