package com.github.maeda6uiui.mechtatel.core.vulkan.cache;

import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.util.FileHashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

/**
 * Utility methods for build cache of shaders
 *
 * @author maeda6uiui
 */
public class ShaderBuildCacheUtils {
    /**
     * Returns build cache of a shader.
     * Returns {@code null} if cache use is disabled or if the cache file does not exist.
     *
     * @return Cache content as a byte array
     * @throws IOException if it fails to load cached content
     */
    public static byte[] retrieve(byte[] srcShaderContent) throws IOException {
        MttSettings settings = MttSettings
                .get()
                .orElse(new MttSettings());
        if (!settings.cacheSettings.useCache) {
            return null;
        }

        String cacheDir = settings.cacheSettings.dirname;
        if (!Files.exists(Paths.get(cacheDir))) {
            return null;
        }

        String md5Hash;
        try {
            md5Hash = FileHashUtils.getFileHash(srcShaderContent, "MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        Path cacheFile = Paths.get(cacheDir, String.format("%s.cache", md5Hash));
        if (!Files.exists(cacheFile)) {
            return null;
        }

        return Files.readAllBytes(cacheFile);
    }
}
