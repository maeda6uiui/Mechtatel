package com.github.maeda6uiui.mechtatel.core.input.mouse;

import com.github.maeda6uiui.mechtatel.core.input.InputCounter;

import java.util.List;

/**
 * Mouse
 *
 * @author maeda
 */
public class Mouse {
    private InputCounter counter;
    private int x;
    private int y;

    public Mouse() {
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

    public void setCursorPos(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getCursorPosX() {
        return x;
    }

    public int getCursorPosY() {
        return y;
    }

    public void update() {
        counter.update();
    }
}
