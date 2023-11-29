package com.github.maeda6uiui.mechtatel.core;

import java.util.List;

/**
 * Interface to {@link MttWindow} providing access to methods relating to draw path settings
 *
 * @author maeda6uiui
 */
public interface IMttWindowForDrawPath {
    void setScreenDrawOrder(List<String> screenDrawOrder);

    void setTextureOperationOrder(List<String> textureOperationOrder);

    void setDeferredScreenDrawOrder(List<String> deferredScreenDrawOrder);

    void setPresentScreenName(String presentScreenName);
}
