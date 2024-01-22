package com.github.maeda6uiui.mechtatel.core.vulkan.frame;

/**
 * Result of frame presentation
 *
 * @author maeda6uiui
 */
public enum PresentResult {
    SUCCESS,
    ACQUIRE_NEXT_IMAGE_OUT_OF_DATE,
    ACQUIRE_NEXT_IMAGE_SUBOPTIMAL,
    QUEUE_PRESENT_OUT_OF_DATE,
    QUEUE_PRESENT_SUBOPTIMAL
}
