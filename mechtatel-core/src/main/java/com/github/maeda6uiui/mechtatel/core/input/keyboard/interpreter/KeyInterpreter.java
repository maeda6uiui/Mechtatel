package com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter;

import com.github.maeda6uiui.mechtatel.core.input.keyboard.KeyCode;

import java.util.Map;

/**
 * Interprets keyboard input and converts it to a letter
 *
 * @author maeda6uiui
 */
public abstract class KeyInterpreter {
    public KeyCode getMinPressingKey(Map<KeyCode, Integer> keyboardPressingCounts) {
        int minPressingCount = Integer.MAX_VALUE;
        KeyCode minPressingKey = KeyCode.UNKNOWN;

        for (var entry : keyboardPressingCounts.entrySet()) {
            KeyCode keyCode = entry.getKey();
            int pressingCount = entry.getValue();
            if (pressingCount > 0 && pressingCount < minPressingCount) {
                minPressingKey = keyCode;
                minPressingCount = pressingCount;
            }
        }

        return minPressingKey;
    }

    public abstract String getInputLetter(Map<KeyCode, Integer> keyboardPressingCounts, int repeatDelayFrames);
}
