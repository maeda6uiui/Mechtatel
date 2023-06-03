package com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter;

import java.util.List;
import java.util.Map;

/**
 * Interprets keyboard input and converts it to a letter
 *
 * @author maeda6uiui
 */
public abstract class KeyInterpreter {
    public String getMinPressingKey(Map<String, Integer> keyboardPressingCounts) {
        int minPressingCount = Integer.MAX_VALUE;
        String minPressingKey = "";
        for (var entry : keyboardPressingCounts.entrySet()) {
            String key = entry.getKey();
            int pressingCount = entry.getValue();

            if (pressingCount > 0 && pressingCount < minPressingCount) {
                minPressingKey = key;
                minPressingCount = pressingCount;
            }
        }

        return minPressingKey;
    }

    public abstract String getInputLetter(
            Map<String, Integer> keyboardPressingCounts,
            List<String> specialKeys,
            int repeatDelayFrames);
}
