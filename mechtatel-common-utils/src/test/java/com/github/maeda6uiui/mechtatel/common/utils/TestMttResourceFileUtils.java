package com.github.maeda6uiui.mechtatel.common.utils;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class TestMttResourceFileUtils {
    @Test
    public void testDeleteTemporaryFiles() throws IOException {
        //Exception is thrown if prefix is null, empty or blank
        assertThrows(IllegalArgumentException.class, () -> {
            MttResourceFileUtils.deleteTemporaryFiles(null, false);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            MttResourceFileUtils.deleteTemporaryFiles("", true);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            MttResourceFileUtils.deleteTemporaryFiles(" ", true);
        });

        //Check if temporary files are deleted
        final String PREFIX = "mtttest";
        Path tempFile = Files.createTempFile(PREFIX, ".tmp");
        Path tempDir = Files.createTempDirectory(PREFIX);
        Files.createTempFile(tempDir, "test_file", ".txt");
        MttResourceFileUtils.deleteTemporaryFiles(PREFIX, false);
        assertFalse(Files.exists(tempFile));
        assertTrue(Files.exists(tempDir));

        //Check if temporary files and directories are deleted
        final String PREFIX_2 = "mtttest2";
        Path tempFile2 = Files.createTempFile(PREFIX_2, ".tmp");
        Path tempDir2 = Files.createTempDirectory(PREFIX_2);
        MttResourceFileUtils.deleteTemporaryFiles(PREFIX_2, true);
        assertFalse(Files.exists(tempFile2));
        assertFalse(Files.exists(tempDir2));
    }
}
