package com.github.maeda6uiui.mechtatel.common.utils;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class TestMttResourceFileUtils {
    private record TempFiles(
            List<Path> tempFiles,
            List<Path> nonTargetTempFiles,
            List<Path> tempDirs,
            List<Path> nonTargetTempDirs) {
    }

    private TempFiles tf;   //Used to delete remaining files after each test case

    private List<Path> generateTempDirs(String tempDirPrefix, int numDirs) throws IOException {
        //Create temporary directories
        var tempDirs = new ArrayList<Path>();
        for (int i = 0; i < numDirs; i++) {
            String prefix;
            if (tempDirPrefix != null) {
                prefix = tempDirPrefix;
            } else {
                prefix = UUID.randomUUID().toString();
            }

            Path tempDir = Files.createTempDirectory(String.format("%s-%d", prefix, i));
            tempDirs.add(tempDir);
        }

        //Populate each directory with a random number of files
        var rnd = new Random();
        for (var tempDir : tempDirs) {
            int numFiles = rnd.nextInt(1, 5);
            for (int i = 0; i < numFiles; i++) {
                Files.createTempFile(tempDir, String.format("file-%d", i), ".tmp");
            }
        }

        return tempDirs;
    }

    private List<Path> generateTempFiles(String tempFilePrefix) throws IOException {
        var tempFiles = new ArrayList<Path>();

        var rnd = new Random();
        int numFiles = rnd.nextInt(5, 10);
        for (int i = 0; i < numFiles; i++) {
            String prefix;
            if (tempFilePrefix != null) {
                prefix = tempFilePrefix;
            } else {
                prefix = UUID.randomUUID().toString();
            }

            Path tempFile = Files.createTempFile(String.format("%s-%d", prefix, i), ".tmp");
            tempFiles.add(tempFile);
        }

        return tempFiles;
    }

    private TempFiles generateTempDirsAndFiles(
            String tempFilePrefix, String tempDirPrefix, int numDirs) throws IOException {
        List<Path> tempFiles = this.generateTempFiles(tempFilePrefix);
        List<Path> nonTargetTempFiles = this.generateTempFiles(null);
        List<Path> tempDirs = this.generateTempDirs(tempDirPrefix, numDirs);
        List<Path> nonTargetTempDirs = this.generateTempDirs(null, numDirs);

        return new TempFiles(tempFiles, nonTargetTempFiles, tempDirs, nonTargetTempDirs);
    }

    @AfterEach
    public void deleteFiles() throws IOException {
        if (tf == null) {
            return;
        }

        List<Path> files = Stream
                .of(tf.tempFiles, tf.nonTargetTempFiles, tf.tempDirs, tf.nonTargetTempDirs)
                .flatMap(List::stream)
                .toList();
        for (var file : files) {
            if (!Files.exists(file)) {
                continue;
            }

            if (Files.isDirectory(file)) {
                FileUtils.deleteDirectory(file.toFile());
            } else {
                Files.delete(file);
            }
        }
    }

    @Test
    public void testDeleteTemporaryFiles_IllegalArgument() {
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
    }

    @Test
    public void testDeleteTemporaryFiles_OnlyFiles() throws IOException {
        tf = this.generateTempDirsAndFiles("mtttest", "mtttest", 5);

        MttResourceFileUtils.deleteTemporaryFiles("mtttest", false);
        for (var tempFile : tf.tempFiles) {
            assertFalse(Files.exists(tempFile));
        }
        for (var nonTargetTempFile : tf.nonTargetTempFiles) {
            assertTrue(Files.exists(nonTargetTempFile));
        }
        for (var tempDir : tf.tempDirs) {
            assertTrue(Files.exists(tempDir));  //Temp directories are not deleted
        }
        for (var nonTargetTempDir : tf.nonTargetTempDirs) {
            assertTrue(Files.exists(nonTargetTempDir));
        }
    }

    @Test
    public void testDeleteTemporaryFiles_FilesAndDirs() throws IOException {
        tf = this.generateTempDirsAndFiles("mtttest", "mtttest", 5);

        MttResourceFileUtils.deleteTemporaryFiles("mtttest", true);
        for (var tempFile : tf.tempFiles) {
            assertFalse(Files.exists(tempFile));
        }
        for (var nonTargetTempFile : tf.nonTargetTempFiles) {
            assertTrue(Files.exists(nonTargetTempFile));
        }
        for (var tempDir : tf.tempDirs) {
            assertFalse(Files.exists(tempDir));  //Temp directories are also deleted
        }
        for (var nonTargetTempDir : tf.nonTargetTempDirs) {
            assertTrue(Files.exists(nonTargetTempDir));
        }
    }
}
