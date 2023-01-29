package com.github.maeda6uiui.mechtatel.core.input.keyboard;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Converts macro-defined values to corresponding strings
 *
 * @author maeda6uiui
 */
class MacroStringConverter {
    public static String convertMacroToString(int macro) {
        String ret;

        switch (macro) {
            case GLFW_KEY_UNKNOWN:
                ret = "UNKNOWN";
                break;
            case GLFW_KEY_SPACE:
                ret = "SPACE";
                break;
            case GLFW_KEY_APOSTROPHE:
                ret = "APOSTROPHE";
                break;
            case GLFW_KEY_COMMA:
                ret = "COMMA";
                break;
            case GLFW_KEY_MINUS:
                ret = "MINUS";
                break;
            case GLFW_KEY_PERIOD:
                ret = "PERIOD";
                break;
            case GLFW_KEY_SLASH:
                ret = "SLASH";
                break;
            case GLFW_KEY_0:
                ret = "0";
                break;
            case GLFW_KEY_1:
                ret = "1";
                break;
            case GLFW_KEY_2:
                ret = "2";
                break;
            case GLFW_KEY_3:
                ret = "3";
                break;
            case GLFW_KEY_4:
                ret = "4";
                break;
            case GLFW_KEY_5:
                ret = "5";
                break;
            case GLFW_KEY_6:
                ret = "6";
                break;
            case GLFW_KEY_7:
                ret = "7";
                break;
            case GLFW_KEY_8:
                ret = "8";
                break;
            case GLFW_KEY_9:
                ret = "9";
                break;
            case GLFW_KEY_SEMICOLON:
                ret = "SEMICOLON";
                break;
            case GLFW_KEY_EQUAL:
                ret = "EQUAL";
                break;
            case GLFW_KEY_A:
                ret = "A";
                break;
            case GLFW_KEY_B:
                ret = "B";
                break;
            case GLFW_KEY_C:
                ret = "C";
                break;
            case GLFW_KEY_D:
                ret = "D";
                break;
            case GLFW_KEY_E:
                ret = "E";
                break;
            case GLFW_KEY_F:
                ret = "F";
                break;
            case GLFW_KEY_G:
                ret = "G";
                break;
            case GLFW_KEY_H:
                ret = "H";
                break;
            case GLFW_KEY_I:
                ret = "I";
                break;
            case GLFW_KEY_J:
                ret = "J";
                break;
            case GLFW_KEY_K:
                ret = "K";
                break;
            case GLFW_KEY_L:
                ret = "L";
                break;
            case GLFW_KEY_M:
                ret = "M";
                break;
            case GLFW_KEY_N:
                ret = "N";
                break;
            case GLFW_KEY_O:
                ret = "O";
                break;
            case GLFW_KEY_P:
                ret = "P";
                break;
            case GLFW_KEY_Q:
                ret = "Q";
                break;
            case GLFW_KEY_R:
                ret = "R";
                break;
            case GLFW_KEY_S:
                ret = "S";
                break;
            case GLFW_KEY_T:
                ret = "T";
                break;
            case GLFW_KEY_U:
                ret = "U";
                break;
            case GLFW_KEY_V:
                ret = "V";
                break;
            case GLFW_KEY_W:
                ret = "W";
                break;
            case GLFW_KEY_X:
                ret = "X";
                break;
            case GLFW_KEY_Y:
                ret = "Y";
                break;
            case GLFW_KEY_Z:
                ret = "Z";
                break;
            case GLFW_KEY_LEFT_BRACKET:
                ret = "LEFT_BRACKET";
                break;
            case GLFW_KEY_BACKSLASH:
                ret = "BACKSLASH";
                break;
            case GLFW_KEY_RIGHT_BRACKET:
                ret = "RIGHT_BRACKET";
                break;
            case GLFW_KEY_GRAVE_ACCENT:
                ret = "GRAVE_ACCENT";
                break;
            case GLFW_KEY_WORLD_1:
                ret = "WORLD_1";
                break;
            case GLFW_KEY_WORLD_2:
                ret = "WORLD_2";
                break;
            case GLFW_KEY_ESCAPE:
                ret = "ESCAPE";
                break;
            case GLFW_KEY_ENTER:
                ret = "ENTER";
                break;
            case GLFW_KEY_TAB:
                ret = "TAB";
                break;
            case GLFW_KEY_BACKSPACE:
                ret = "BACKSPACE";
                break;
            case GLFW_KEY_INSERT:
                ret = "INSERT";
                break;
            case GLFW_KEY_DELETE:
                ret = "DELETE";
                break;
            case GLFW_KEY_RIGHT:
                ret = "RIGHT";
                break;
            case GLFW_KEY_LEFT:
                ret = "LEFT";
                break;
            case GLFW_KEY_DOWN:
                ret = "DOWN";
                break;
            case GLFW_KEY_UP:
                ret = "UP";
                break;
            case GLFW_KEY_PAGE_UP:
                ret = "PAGE_UP";
                break;
            case GLFW_KEY_PAGE_DOWN:
                ret = "PAGE_DOWN";
                break;
            case GLFW_KEY_HOME:
                ret = "HOME";
                break;
            case GLFW_KEY_END:
                ret = "END";
                break;
            case GLFW_KEY_CAPS_LOCK:
                ret = "CAPS_LOCK";
                break;
            case GLFW_KEY_SCROLL_LOCK:
                ret = "SCROLL_LOCK";
                break;
            case GLFW_KEY_NUM_LOCK:
                ret = "NUM_LOCK";
                break;
            case GLFW_KEY_PRINT_SCREEN:
                ret = "PRINT_SCREEN";
                break;
            case GLFW_KEY_PAUSE:
                ret = "PAUSE";
                break;
            case GLFW_KEY_F1:
                ret = "F1";
                break;
            case GLFW_KEY_F2:
                ret = "F2";
                break;
            case GLFW_KEY_F3:
                ret = "F3";
                break;
            case GLFW_KEY_F4:
                ret = "F4";
                break;
            case GLFW_KEY_F5:
                ret = "F5";
                break;
            case GLFW_KEY_F6:
                ret = "F6";
                break;
            case GLFW_KEY_F7:
                ret = "F7";
                break;
            case GLFW_KEY_F8:
                ret = "F8";
                break;
            case GLFW_KEY_F9:
                ret = "F9";
                break;
            case GLFW_KEY_F10:
                ret = "F10";
                break;
            case GLFW_KEY_F11:
                ret = "F11";
                break;
            case GLFW_KEY_F12:
                ret = "F12";
                break;
            case GLFW_KEY_F13:
                ret = "F13";
                break;
            case GLFW_KEY_F14:
                ret = "F14";
                break;
            case GLFW_KEY_F15:
                ret = "F15";
                break;
            case GLFW_KEY_F16:
                ret = "F16";
                break;
            case GLFW_KEY_F17:
                ret = "F17";
                break;
            case GLFW_KEY_F18:
                ret = "F18";
                break;
            case GLFW_KEY_F19:
                ret = "F19";
                break;
            case GLFW_KEY_F20:
                ret = "F20";
                break;
            case GLFW_KEY_F21:
                ret = "F21";
                break;
            case GLFW_KEY_F22:
                ret = "F22";
                break;
            case GLFW_KEY_F23:
                ret = "F23";
                break;
            case GLFW_KEY_F24:
                ret = "F24";
                break;
            case GLFW_KEY_F25:
                ret = "F25";
                break;
            case GLFW_KEY_KP_0:
                ret = "KP_0";
                break;
            case GLFW_KEY_KP_1:
                ret = "KP_1";
                break;
            case GLFW_KEY_KP_2:
                ret = "KP_2";
                break;
            case GLFW_KEY_KP_3:
                ret = "KP_3";
                break;
            case GLFW_KEY_KP_4:
                ret = "KP_4";
                break;
            case GLFW_KEY_KP_5:
                ret = "KP_5";
                break;
            case GLFW_KEY_KP_6:
                ret = "KP_6";
                break;
            case GLFW_KEY_KP_7:
                ret = "KP_7";
                break;
            case GLFW_KEY_KP_8:
                ret = "KP_8";
                break;
            case GLFW_KEY_KP_9:
                ret = "KP_9";
                break;
            case GLFW_KEY_KP_DECIMAL:
                ret = "KP_DECIMAL";
                break;
            case GLFW_KEY_KP_DIVIDE:
                ret = "KP_DIVIDE";
                break;
            case GLFW_KEY_KP_MULTIPLY:
                ret = "KP_MULTIPLY";
                break;
            case GLFW_KEY_KP_SUBTRACT:
                ret = "KP_SUBTRACT";
                break;
            case GLFW_KEY_KP_ADD:
                ret = "KP_ADD";
                break;
            case GLFW_KEY_KP_ENTER:
                ret = "KP_ENTER";
                break;
            case GLFW_KEY_KP_EQUAL:
                ret = "KP_EQUAL";
                break;
            case GLFW_KEY_LEFT_SHIFT:
                ret = "LEFT_SHIFT";
                break;
            case GLFW_KEY_LEFT_CONTROL:
                ret = "LEFT_CONTROL";
                break;
            case GLFW_KEY_LEFT_ALT:
                ret = "LEFT_ALT";
                break;
            case GLFW_KEY_LEFT_SUPER:
                ret = "LEFT_SUPER";
                break;
            case GLFW_KEY_RIGHT_SHIFT:
                ret = "RIGHT_SHIFT";
                break;
            case GLFW_KEY_RIGHT_CONTROL:
                ret = "RIGHT_CONTROL";
                break;
            case GLFW_KEY_RIGHT_ALT:
                ret = "RIGHT_ALT";
                break;
            case GLFW_KEY_RIGHT_SUPER:
                ret = "RIGHT_SUPER";
                break;
            case GLFW_KEY_MENU:
                ret = "MENU";
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
        macros.add(GLFW_KEY_UNKNOWN);
        macros.add(GLFW_KEY_SPACE);
        macros.add(GLFW_KEY_APOSTROPHE);
        macros.add(GLFW_KEY_COMMA);
        macros.add(GLFW_KEY_MINUS);
        macros.add(GLFW_KEY_PERIOD);
        macros.add(GLFW_KEY_SLASH);
        macros.add(GLFW_KEY_0);
        macros.add(GLFW_KEY_1);
        macros.add(GLFW_KEY_2);
        macros.add(GLFW_KEY_3);
        macros.add(GLFW_KEY_4);
        macros.add(GLFW_KEY_5);
        macros.add(GLFW_KEY_6);
        macros.add(GLFW_KEY_7);
        macros.add(GLFW_KEY_8);
        macros.add(GLFW_KEY_9);
        macros.add(GLFW_KEY_SEMICOLON);
        macros.add(GLFW_KEY_EQUAL);
        macros.add(GLFW_KEY_A);
        macros.add(GLFW_KEY_B);
        macros.add(GLFW_KEY_C);
        macros.add(GLFW_KEY_D);
        macros.add(GLFW_KEY_E);
        macros.add(GLFW_KEY_F);
        macros.add(GLFW_KEY_G);
        macros.add(GLFW_KEY_H);
        macros.add(GLFW_KEY_I);
        macros.add(GLFW_KEY_J);
        macros.add(GLFW_KEY_K);
        macros.add(GLFW_KEY_L);
        macros.add(GLFW_KEY_M);
        macros.add(GLFW_KEY_N);
        macros.add(GLFW_KEY_O);
        macros.add(GLFW_KEY_P);
        macros.add(GLFW_KEY_Q);
        macros.add(GLFW_KEY_R);
        macros.add(GLFW_KEY_S);
        macros.add(GLFW_KEY_T);
        macros.add(GLFW_KEY_U);
        macros.add(GLFW_KEY_V);
        macros.add(GLFW_KEY_W);
        macros.add(GLFW_KEY_X);
        macros.add(GLFW_KEY_Y);
        macros.add(GLFW_KEY_Z);
        macros.add(GLFW_KEY_LEFT_BRACKET);
        macros.add(GLFW_KEY_BACKSLASH);
        macros.add(GLFW_KEY_RIGHT_BRACKET);
        macros.add(GLFW_KEY_GRAVE_ACCENT);
        macros.add(GLFW_KEY_WORLD_1);
        macros.add(GLFW_KEY_WORLD_2);
        macros.add(GLFW_KEY_ESCAPE);
        macros.add(GLFW_KEY_ENTER);
        macros.add(GLFW_KEY_TAB);
        macros.add(GLFW_KEY_BACKSPACE);
        macros.add(GLFW_KEY_INSERT);
        macros.add(GLFW_KEY_DELETE);
        macros.add(GLFW_KEY_RIGHT);
        macros.add(GLFW_KEY_LEFT);
        macros.add(GLFW_KEY_DOWN);
        macros.add(GLFW_KEY_UP);
        macros.add(GLFW_KEY_PAGE_UP);
        macros.add(GLFW_KEY_PAGE_DOWN);
        macros.add(GLFW_KEY_HOME);
        macros.add(GLFW_KEY_END);
        macros.add(GLFW_KEY_CAPS_LOCK);
        macros.add(GLFW_KEY_SCROLL_LOCK);
        macros.add(GLFW_KEY_NUM_LOCK);
        macros.add(GLFW_KEY_PRINT_SCREEN);
        macros.add(GLFW_KEY_PAUSE);
        macros.add(GLFW_KEY_F1);
        macros.add(GLFW_KEY_F2);
        macros.add(GLFW_KEY_F3);
        macros.add(GLFW_KEY_F4);
        macros.add(GLFW_KEY_F5);
        macros.add(GLFW_KEY_F6);
        macros.add(GLFW_KEY_F7);
        macros.add(GLFW_KEY_F8);
        macros.add(GLFW_KEY_F9);
        macros.add(GLFW_KEY_F10);
        macros.add(GLFW_KEY_F11);
        macros.add(GLFW_KEY_F12);
        macros.add(GLFW_KEY_F13);
        macros.add(GLFW_KEY_F14);
        macros.add(GLFW_KEY_F15);
        macros.add(GLFW_KEY_F16);
        macros.add(GLFW_KEY_F17);
        macros.add(GLFW_KEY_F18);
        macros.add(GLFW_KEY_F19);
        macros.add(GLFW_KEY_F20);
        macros.add(GLFW_KEY_F21);
        macros.add(GLFW_KEY_F22);
        macros.add(GLFW_KEY_F23);
        macros.add(GLFW_KEY_F24);
        macros.add(GLFW_KEY_F25);
        macros.add(GLFW_KEY_KP_0);
        macros.add(GLFW_KEY_KP_1);
        macros.add(GLFW_KEY_KP_2);
        macros.add(GLFW_KEY_KP_3);
        macros.add(GLFW_KEY_KP_4);
        macros.add(GLFW_KEY_KP_5);
        macros.add(GLFW_KEY_KP_6);
        macros.add(GLFW_KEY_KP_7);
        macros.add(GLFW_KEY_KP_8);
        macros.add(GLFW_KEY_KP_9);
        macros.add(GLFW_KEY_KP_DECIMAL);
        macros.add(GLFW_KEY_KP_DIVIDE);
        macros.add(GLFW_KEY_KP_MULTIPLY);
        macros.add(GLFW_KEY_KP_SUBTRACT);
        macros.add(GLFW_KEY_KP_ADD);
        macros.add(GLFW_KEY_KP_ENTER);
        macros.add(GLFW_KEY_KP_EQUAL);
        macros.add(GLFW_KEY_LEFT_SHIFT);
        macros.add(GLFW_KEY_LEFT_CONTROL);
        macros.add(GLFW_KEY_LEFT_ALT);
        macros.add(GLFW_KEY_LEFT_SUPER);
        macros.add(GLFW_KEY_RIGHT_SHIFT);
        macros.add(GLFW_KEY_RIGHT_CONTROL);
        macros.add(GLFW_KEY_RIGHT_ALT);
        macros.add(GLFW_KEY_RIGHT_SUPER);
        macros.add(GLFW_KEY_MENU);

        macros.forEach(macro -> {
            var macroString = convertMacroToString(macro);
            macroStrings.add(macroString);
        });

        return macroStrings;
    }
}
