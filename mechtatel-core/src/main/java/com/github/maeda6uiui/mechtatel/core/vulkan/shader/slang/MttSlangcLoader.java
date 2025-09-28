package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import com.github.maeda6uiui.mechtatel.natives.IMttNativeLoader;
import com.github.maeda6uiui.mechtatel.natives.MttNativeLoaderFactory;
import com.sun.jna.Native;
import com.sun.jna.Platform;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.*;

/**
 * Native library loader for the Slang compiler
 *
 * @author maeda6uiui
 */
class MttSlangcLoader {
    private static final Logger logger = LoggerFactory.getLogger(MttSlangcLoader.class);

    public static IMttSlangc load() {
        String platform;
        if (Platform.isWindows()) {
            platform = "windows";
        } else if (Platform.isLinux()) {
            platform = "linux";
        } else {
            throw new RuntimeException("Unsupported platform");
        }

        final String tmpDirPrefix = "mttnatives";

        //Delete previously created temporary directories
        Path tempRoot = Paths.get(System.getProperty("java.io.tmpdir"));
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(tempRoot, tmpDirPrefix + "*")) {
            stream.forEach(p -> {
                if (Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS) && p.startsWith(tempRoot)) {
                    try {
                        FileUtils.deleteDirectory(p.toFile());
                    } catch (IOException e) {
                        logger.warn("Failed to delete temporary directory", e);
                    }
                }
            });
        } catch (IOException e) {
            logger.warn("Failed to delete temporary directory", e);
        }

        //Create a temporary directory to extract native libs into
        Path tempDir;
        try {
            tempDir = Files.createTempDirectory(tmpDirPrefix);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //Extract native libs
        File slangLibFile;
        File mttslangcLibFile;
        try {
            IMttNativeLoader loader = MttNativeLoaderFactory.createNativeLoader(platform);
            slangLibFile = loader.extractLibSlang(tempDir);
            mttslangcLibFile = loader.extractLibMttSlangc(tempDir);
        } catch (
                ClassNotFoundException
                | NoSuchMethodException
                | InstantiationException
                | IllegalAccessException
                | InvocationTargetException
                | IOException e) {
            throw new RuntimeException(e);
        }

        //Load native libs
        System.load(slangLibFile.getPath());
        return Native.load(mttslangcLibFile.getPath(), IMttSlangc.class);
    }
}
