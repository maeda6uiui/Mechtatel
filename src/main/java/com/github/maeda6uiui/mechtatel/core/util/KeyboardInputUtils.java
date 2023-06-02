package com.github.maeda6uiui.mechtatel.core.util;

import java.util.Map;

/**
 * Utility methods for keyboard input
 *
 * @author maeda6uiui
 */
public class KeyboardInputUtils {
    public static String getInputLetter(
            Map<String, Integer> keyboardPressingCounts,
            Map<String, String> shiftKeyCombinations,
            int repeatDelayFrames) {
        //The key that has been pressed most recently is the one that should be interpreted
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

        if (minPressingKey.equals("")) {
            return "";
        }

        if (!(minPressingCount == 1 || minPressingCount > repeatDelayFrames)) {
            return "";
        }

        //Check what letter should be output
        boolean shiftPressed = false;
        if (keyboardPressingCounts.get("LEFT_SHIFT") > 0 || keyboardPressingCounts.get("RIGHT_SHIFT") > 0) {
            shiftPressed = true;
        }

        String outputLetter = "";
    }
}
