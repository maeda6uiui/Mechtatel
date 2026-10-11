package com.github.maeda6uiui.mechtatel.audio;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NativeLoaderTest {
    //Columns: windows, linux, mac, intel, arm
    @ParameterizedTest
    @CsvSource({
            "true,false,false,true,false,windows",
            "true,false,false,false,true,windowsarm64",
            "false,true,false,true,false,linux",
            "false,true,false,false,true,linuxarm64",
            "false,false,true,false,true,macosarm64"
    })
    void resolvesSupportedPlatforms(
            boolean windows, boolean linux, boolean mac, boolean intel, boolean arm, String expected) {
        assertEquals(expected, NativeLoader.resolvePlatform(windows, linux, mac, intel, arm));
    }

    @ParameterizedTest
    @CsvSource({
            "true,false,false,false,false",
            "false,true,false,false,false",
            "false,false,true,true,false",
            "false,false,true,false,false",
            "false,false,false,true,false"
    })
    void rejectsUnsupportedPlatforms(boolean windows, boolean linux, boolean mac, boolean intel, boolean arm) {
        assertThrows(RuntimeException.class, () -> NativeLoader.resolvePlatform(windows, linux, mac, intel, arm));
    }
}
