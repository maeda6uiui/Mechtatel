package com.github.maeda6uiui.mechtatel.core.component.gui;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default callbacks for listbox item
 *
 * @author maeda6uiui
 */
public class MttListboxItemDefaultCallbacks extends MttGuiComponentCallbacks {
    private final Logger logger = LoggerFactory.getLogger(MttListboxItemDefaultCallbacks.class);

    private int itemIndex;

    public MttListboxItemDefaultCallbacks(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    @Override
    public void onLButtonDown() {
        logger.info("Item #{} selected", itemIndex);
    }
}
