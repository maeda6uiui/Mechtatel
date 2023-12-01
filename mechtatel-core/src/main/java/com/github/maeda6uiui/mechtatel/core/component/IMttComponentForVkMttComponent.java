package com.github.maeda6uiui.mechtatel.core.component;

import org.joml.Matrix4fc;

/**
 * Interface of {@link MttComponent} for Vulkan implementation of components
 *
 * @author maeda6uiui
 */
public interface IMttComponentForVkMttComponent {
    String getTag();

    Matrix4fc getMat();

    boolean isVisible();

    int getDrawOrder();

    boolean isTwoDComponent();

    boolean shouldCastShadow();
}
