package com.github.maeda6uiui.mechtatel.core.input.keyboard;

import com.github.maeda6uiui.mechtatel.core.input.InputCounter;

import java.util.List;

/**
 * Keyboard
 *
 * @author maeda6uiui
 */
public class Keyboard {
    private InputCounter counter;

    public Keyboard() {
        List<String> keys = MacroStringConverter.getAllMacroStrings();
        counter = new InputCounter(keys);
    }

    public int getPressingCount(String key) {
        return counter.getPressingCount(key);
    }

    public int getReleasingCount(String key) {
        return counter.getReleasingCount(key);
    }

    public void setPressingFlag(int keyCode, boolean pressingFlag) {
        String key = MacroStringConverter.convertMacroToString(keyCode);
        counter.setPressingFlag(key, pressingFlag);
    }

    public void update() {
        counter.update();
    }
}
