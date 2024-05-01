package com.github.maeda6uiui.mechtatel.core.screen.component;

import com.github.maeda6uiui.mechtatel.core.model.MttAnimationData;
import com.github.maeda6uiui.mechtatel.core.model.MttModelData;

/**
 * Interface of {@link MttModel} for {@link com.github.maeda6uiui.mechtatel.core.vulkan.screen.component.VkMttModel}
 *
 * @author maeda6uiui
 */
public interface IMttModelForVkMttModel {
    MttModelData getModelData();

    MttAnimationData getAnimationData();
}
