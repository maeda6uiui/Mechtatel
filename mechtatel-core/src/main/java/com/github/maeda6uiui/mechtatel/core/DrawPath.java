package com.github.maeda6uiui.mechtatel.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Draw path
 *
 * @author maeda6uiui
 */
public class DrawPath {
    private IMttWindowForDrawPath mtt;

    private List<String> screenDrawOrder;
    private List<String> textureOperationOrder;
    private List<String> deferredScreenDrawOrder;
    private String presentScreenName;

    public DrawPath(IMttWindowForDrawPath mtt) {
        this.mtt = mtt;

        screenDrawOrder = new ArrayList<>();
        textureOperationOrder = new ArrayList<>();
        deferredScreenDrawOrder = new ArrayList<>();
        presentScreenName = "default";
    }

    public void reset() {
        screenDrawOrder.clear();
        textureOperationOrder.clear();
        deferredScreenDrawOrder.clear();
        presentScreenName = "default";
    }

    public void addToScreenDrawOrder(String screenName) {
        screenDrawOrder.add(screenName);
    }

    public void addToTextureOperationOrder(String operationName) {
        textureOperationOrder.add(operationName);
    }

    public void addToDeferredScreenDrawOrder(String screenName) {
        deferredScreenDrawOrder.add(screenName);
    }

    public void setPresentScreenName(String screenName) {
        presentScreenName = screenName;
    }

    public void apply() {
        mtt.setScreenDrawOrder(screenDrawOrder);
        mtt.setTextureOperationOrder(textureOperationOrder);
        mtt.setDeferredScreenDrawOrder(deferredScreenDrawOrder);
        mtt.setPresentScreenName(presentScreenName);
    }
}
