package com.github.maeda6uiui.mechtatel.core.vulkan.shader.slang;

import java.nio.file.Path;

/**
 * Interface to {@link SlangShaderExtractor} providing access to its getters
 *
 * @author maeda6uiui
 */
public interface ISlangShaderExtractorGetters {
    String getAllSourcesConcatenated();

    Path getEntryPointPath();
}
