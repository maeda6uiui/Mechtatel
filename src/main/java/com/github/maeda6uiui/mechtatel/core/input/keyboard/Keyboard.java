package com.github.maeda6uiui.mechtatel.core.input.keyboard;

import com.github.maeda6uiui.mechtatel.core.input.InputCounter;

import java.util.List;
import java.util.Map;

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

    public Map<String, Integer> getPressingCounts() {
        return counter.getPressingCounts();
    }

    public int getReleasingCount(String key) {
        return counter.getReleasingCount(key);
    }

    public Map<String, Integer> getReleasingCounts() {
        return counter.getReleasingCounts();
    }

    public void setPressingFlag(int keyCode, boolean pressingFlag) {
        String key = MacroStringConverter.convertMacroToString(keyCode);
        counter.setPressingFlag(key, pressingFlag);
    }

    public void update() {
        counter.update();
    }
}
