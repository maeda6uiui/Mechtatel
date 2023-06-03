package com.github.maeda6uiui.mechtatel.core.input.keyboard.interpreter;

import java.util.List;
import java.util.Map;

/**
 * Interprets keyboard input and converts it to a letter
 *
 * @author maeda6uiui
 */
public abstract class KeyInterpreter {
    public abstract String getInputLetter(
            Map<String, Integer> keyboardPressingCounts,
            List<String> specialKeys,
            int repeatDelayFrames);
}
