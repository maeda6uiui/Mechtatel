package com.github.maeda6uiui.mechtatel.core.input.mouse;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Converts macro-defined values to corresponding strings
 *
 * @author maeda
 */
class MacroStringConverter {
    public static String convertMacroToString(int macro) {
        String ret;

        switch (macro) {
            case GLFW_MOUSE_BUTTON_1:
                ret = "BUTTON_LEFT";
                break;
            case GLFW_MOUSE_BUTTON_2:
                ret = "BUTTON_RIGHT";
                break;
            case GLFW_MOUSE_BUTTON_3:
                ret = "BUTTON_MIDDLE";
                break;
            case GLFW_MOUSE_BUTTON_4:
                ret = "BUTTON_4";
                break;
            case GLFW_MOUSE_BUTTON_5:
                ret = "BUTTON_5";
                break;
            case GLFW_MOUSE_BUTTON_6:
                ret = "BUTTON_6";
                break;
            case GLFW_MOUSE_BUTTON_7:
                ret = "BUTTON_7";
                break;
            case GLFW_MOUSE_BUTTON_8:
                ret = "BUTTON_8";
                break;
            default:
                ret = "UNKNOWN";
                break;
        }

        return ret;
    }

    public static List<String> getAllMacroStrings() {
        var macroStrings = new ArrayList<String>();

        var macros = new ArrayList<Integer>();
        macros.add(GLFW_MOUSE_BUTTON_1);
        macros.add(GLFW_MOUSE_BUTTON_2);
        macros.add(GLFW_MOUSE_BUTTON_3);
        macros.add(GLFW_MOUSE_BUTTON_4);
        macros.add(GLFW_MOUSE_BUTTON_5);
        macros.add(GLFW_MOUSE_BUTTON_6);
        macros.add(GLFW_MOUSE_BUTTON_7);
        macros.add(GLFW_MOUSE_BUTTON_8);

        macros.forEach(macro -> {
            var macroString = convertMacroToString(macro);
            macroStrings.add(macroString);
        });

        return macroStrings;
    }
}
