package com.github.maeda6uiui.mechtatel.core.vulkan;

/**
 * Queue family indices
 *
 * @author maeda
 */
class QueueFamilyIndices {
    public Integer graphicsFamily;
    public Integer presentFamily;

    public boolean isComplete() {
        return graphicsFamily != null && presentFamily != null;
    }
}
