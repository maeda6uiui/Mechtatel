package com.github.maeda6uiui.mechtatel.core.vulkan.cache;

import com.github.maeda6uiui.mechtatel.core.MttSettings;
import com.github.maeda6uiui.mechtatel.core.util.FileHashUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;

/**
 * Manager for build cache of shaders
 *
 * @author maeda6uiui
 */
public class ShaderBuildCacheManager {
    private boolean useCache;
    private Path cacheDir;
    private String srcShaderMD5Hash;

    public ShaderBuildCacheManager(byte[] srcShaderContent) throws IOException {
        MttSettings settings = MttSettings
                .get()
                .orElse(new MttSettings());
        useCache = settings.cacheSettings.useCache;
        if (!useCache) {
            return;
        }

        cacheDir = Paths.get(settings.cacheSettings.dirname);

        try {
            srcShaderMD5Hash = FileHashUtils.getFileHash(srcShaderContent, "MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns build cache of a shader.
     * Returns {@code null} if cache use is disabled or if the cache file does not exist.
     *
     * @return Cache content as a byte array
     * @throws IOException if it fails to load cache content
     */
    public byte[] retrieve() throws IOException {
        if (!useCache) {
            return null;
        }
        if (!Files.exists(cacheDir)) {
            return null;
        }

        Path cacheFile = cacheDir.resolve(String.format("%s.cache", srcShaderMD5Hash));
        if (!Files.exists(cacheFile)) {
            return null;
        }

        return Files.readAllBytes(cacheFile);
    }

    /**
     * Saves build cache.
     *
     * @param buildCacheContent Build cache content
     * @throws IOException if it fails to save cache content
     */
    public void save(byte[] buildCacheContent) throws IOException {
        if (!useCache) {
            return;
        }
        if (!Files.exists(cacheDir)) {
            Files.createDirectories(cacheDir);
        }

        Path cacheFile = cacheDir.resolve(String.format("%s.cache", srcShaderMD5Hash));
        Files.write(cacheFile, buildCacheContent);
    }
}
