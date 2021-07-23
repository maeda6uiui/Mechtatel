package com.github.maeda6uiui.mechtatel.core;

/**
 * Queue family indices
 *
 * @author maeda
 */
class QueueFamilyIndices {
    public Integer graphicsFamily;

    public boolean isComplete() {
        return graphicsFamily != null;
    }
}
