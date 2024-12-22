package com.github.maeda6uiui.mechtatel.core.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility methods to calculate file hash
 *
 * @author maeda6uiui
 */
public class FileHashUtils {
    public static String getFileHash(byte[] bin, String algorithmName) throws IOException, NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(algorithmName);

        byte[] hash;
        try (var dis = new DigestInputStream(new BufferedInputStream(new ByteArrayInputStream(bin)), md)) {
            while (dis.read() != -1) {
            }

            hash = md.digest();
        }

        var sb = new StringBuilder();
        for (var b : hash) {
            String hex = String.format("%02x", b);
            sb.append(hex);
        }

        return sb.toString();
    }

    public static String getFileHash(Path path, String algorithmName) throws IOException, NoSuchAlgorithmException {
        byte[] bin = Files.readAllBytes(path);
        return getFileHash(bin, algorithmName);
    }
}
