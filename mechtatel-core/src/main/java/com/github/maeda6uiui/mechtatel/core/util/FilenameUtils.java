package com.github.maeda6uiui.mechtatel.core.util;

/**
 * Utility methods for operations related to filenames
 *
 * @author maeda6uiui
 */
public class FilenameUtils {
    /**
     * Retunrs the file extension.
     * Returns an empty string if the filename specified does not have an extension.
     *
     * @param filename Filename
     * @return File extension
     */
    public static String getFileExtension(String filename) {
        int lastDotPos = filename.lastIndexOf('.');
        if (lastDotPos == -1) {
            return "";
        }

        if (lastDotPos == filename.length() - 1) {
            return "";
        }

        return filename.substring(lastDotPos + 1);
    }
}
