package com.github.maeda6uiui.mechtatel.hello;

import com.github.maeda6uiui.mechtatel.core.MttHeadlessInstance;

import java.nio.file.Path;

/**
 * Interface to event handlers
 *
 * @author maeda6uiui
 */
interface IMttHello {
    void onInit(MttHeadlessInstance instance);

    void onUpdate(MttHeadlessInstance instance, Path imageOutputPath);
}
