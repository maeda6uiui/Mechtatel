package com.github.maeda6uiui.mechtatel.core.vulkan.nabor;

import java.util.List;

/**
 * Info for texture operation
 *
 * @author maeda6uiui
 */
public record TextureOperationInfo(
        List<Long> colorImageViews,
        long dstImage,
        long dstImageView) {
}
