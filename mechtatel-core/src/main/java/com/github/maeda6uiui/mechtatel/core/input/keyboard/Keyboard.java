package com.github.maeda6uiui.mechtatel.core.input.keyboard;

import com.github.maeda6uiui.mechtatel.core.input.InputCounter;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Keyboard
 *
 * @author maeda6uiui
 */
public class Keyboard {
    private static KeyCode convertKeyCodeToEnum(int keyCode) {
        return switch (keyCode) {
            case GLFW_KEY_SPACE -> KeyCode.SPACE;
            case GLFW_KEY_APOSTROPHE -> KeyCode.APOSTROPHE;
            case GLFW_KEY_COMMA -> KeyCode.COMMA;
            case GLFW_KEY_MINUS -> KeyCode.MINUS;
            case GLFW_KEY_PERIOD -> KeyCode.PERIOD;
            case GLFW_KEY_SLASH -> KeyCode.SLASH;
            case GLFW_KEY_0 -> KeyCode.KEY_0;
            case GLFW_KEY_1 -> KeyCode.KEY_1;
            case GLFW_KEY_2 -> KeyCode.KEY_2;
            case GLFW_KEY_3 -> KeyCode.KEY_3;
            case GLFW_KEY_4 -> KeyCode.KEY_4;
            case GLFW_KEY_5 -> KeyCode.KEY_5;
            case GLFW_KEY_6 -> KeyCode.KEY_6;
            case GLFW_KEY_7 -> KeyCode.KEY_7;
            case GLFW_KEY_8 -> KeyCode.KEY_8;
            case GLFW_KEY_9 -> KeyCode.KEY_9;
            case GLFW_KEY_SEMICOLON -> KeyCode.SEMICOLON;
            case GLFW_KEY_EQUAL -> KeyCode.EQUAL;
            case GLFW_KEY_A -> KeyCode.A;
            case GLFW_KEY_B -> KeyCode.B;
            case GLFW_KEY_C -> KeyCode.C;
            case GLFW_KEY_D -> KeyCode.D;
            case GLFW_KEY_E -> KeyCode.E;
            case GLFW_KEY_F -> KeyCode.F;
            case GLFW_KEY_G -> KeyCode.G;
            case GLFW_KEY_H -> KeyCode.H;
            case GLFW_KEY_I -> KeyCode.I;
            case GLFW_KEY_J -> KeyCode.J;
            case GLFW_KEY_K -> KeyCode.K;
            case GLFW_KEY_L -> KeyCode.L;
            case GLFW_KEY_M -> KeyCode.M;
            case GLFW_KEY_N -> KeyCode.N;
            case GLFW_KEY_O -> KeyCode.O;
            case GLFW_KEY_P -> KeyCode.P;
            case GLFW_KEY_Q -> KeyCode.Q;
            case GLFW_KEY_R -> KeyCode.R;
            case GLFW_KEY_S -> KeyCode.S;
            case GLFW_KEY_T -> KeyCode.T;
            case GLFW_KEY_U -> KeyCode.U;
            case GLFW_KEY_V -> KeyCode.V;
            case GLFW_KEY_W -> KeyCode.W;
            case GLFW_KEY_X -> KeyCode.X;
            case GLFW_KEY_Y -> KeyCode.Y;
            case GLFW_KEY_Z -> KeyCode.Z;
            case GLFW_KEY_LEFT_BRACKET -> KeyCode.LEFT_BRACKET;
            case GLFW_KEY_BACKSLASH -> KeyCode.BACKSLASH;
            case GLFW_KEY_RIGHT_BRACKET -> KeyCode.RIGHT_BRACKET;
            case GLFW_KEY_GRAVE_ACCENT -> KeyCode.GRAVE_ACCENT;
            case GLFW_KEY_WORLD_1 -> KeyCode.WORLD_1;
            case GLFW_KEY_WORLD_2 -> KeyCode.WORLD_2;
            case GLFW_KEY_ESCAPE -> KeyCode.ESCAPE;
            case GLFW_KEY_ENTER -> KeyCode.ENTER;
            case GLFW_KEY_TAB -> KeyCode.TAB;
            case GLFW_KEY_BACKSPACE -> KeyCode.BACKSPACE;
            case GLFW_KEY_INSERT -> KeyCode.INSERT;
            case GLFW_KEY_DELETE -> KeyCode.DELETE;
            case GLFW_KEY_RIGHT -> KeyCode.RIGHT;
            case GLFW_KEY_LEFT -> KeyCode.LEFT;
            case GLFW_KEY_DOWN -> KeyCode.DOWN;
            case GLFW_KEY_UP -> KeyCode.UP;
            case GLFW_KEY_PAGE_UP -> KeyCode.PAGE_UP;
            case GLFW_KEY_PAGE_DOWN -> KeyCode.PAGE_DOWN;
            case GLFW_KEY_HOME -> KeyCode.HOME;
            case GLFW_KEY_END -> KeyCode.END;
            case GLFW_KEY_CAPS_LOCK -> KeyCode.CAPS_LOCK;
            case GLFW_KEY_SCROLL_LOCK -> KeyCode.SCROLL_LOCK;
            case GLFW_KEY_NUM_LOCK -> KeyCode.NUM_LOCK;
            case GLFW_KEY_PRINT_SCREEN -> KeyCode.PRINT_SCREEN;
            case GLFW_KEY_PAUSE -> KeyCode.PAUSE;
            case GLFW_KEY_F1 -> KeyCode.F1;
            case GLFW_KEY_F2 -> KeyCode.F2;
            case GLFW_KEY_F3 -> KeyCode.F3;
            case GLFW_KEY_F4 -> KeyCode.F4;
            case GLFW_KEY_F5 -> KeyCode.F5;
            case GLFW_KEY_F6 -> KeyCode.F6;
            case GLFW_KEY_F7 -> KeyCode.F7;
            case GLFW_KEY_F8 -> KeyCode.F8;
            case GLFW_KEY_F9 -> KeyCode.F9;
            case GLFW_KEY_F10 -> KeyCode.F10;
            case GLFW_KEY_F11 -> KeyCode.F11;
            case GLFW_KEY_F12 -> KeyCode.F12;
            case GLFW_KEY_F13 -> KeyCode.F13;
            case GLFW_KEY_F14 -> KeyCode.F14;
            case GLFW_KEY_F15 -> KeyCode.F15;
            case GLFW_KEY_F16 -> KeyCode.F16;
            case GLFW_KEY_F17 -> KeyCode.F17;
            case GLFW_KEY_F18 -> KeyCode.F18;
            case GLFW_KEY_F19 -> KeyCode.F19;
            case GLFW_KEY_F20 -> KeyCode.F20;
            case GLFW_KEY_F21 -> KeyCode.F21;
            case GLFW_KEY_F22 -> KeyCode.F22;
            case GLFW_KEY_F23 -> KeyCode.F23;
            case GLFW_KEY_F24 -> KeyCode.F24;
            case GLFW_KEY_F25 -> KeyCode.F25;
            case GLFW_KEY_KP_0 -> KeyCode.KP_0;
            case GLFW_KEY_KP_1 -> KeyCode.KP_1;
            case GLFW_KEY_KP_2 -> KeyCode.KP_2;
            case GLFW_KEY_KP_3 -> KeyCode.KP_3;
            case GLFW_KEY_KP_4 -> KeyCode.KP_4;
            case GLFW_KEY_KP_5 -> KeyCode.KP_5;
            case GLFW_KEY_KP_6 -> KeyCode.KP_6;
            case GLFW_KEY_KP_7 -> KeyCode.KP_7;
            case GLFW_KEY_KP_8 -> KeyCode.KP_8;
            case GLFW_KEY_KP_9 -> KeyCode.KP_9;
            case GLFW_KEY_KP_DECIMAL -> KeyCode.KP_DECIMAL;
            case GLFW_KEY_KP_DIVIDE -> KeyCode.KP_DIVIDE;
            case GLFW_KEY_KP_MULTIPLY -> KeyCode.KP_MULTIPLY;
            case GLFW_KEY_KP_SUBTRACT -> KeyCode.KP_SUBTRACT;
            case GLFW_KEY_KP_ADD -> KeyCode.KP_ADD;
            case GLFW_KEY_KP_ENTER -> KeyCode.KP_ENTER;
            case GLFW_KEY_KP_EQUAL -> KeyCode.KP_EQUAL;
            case GLFW_KEY_LEFT_SHIFT -> KeyCode.LEFT_SHIFT;
            case GLFW_KEY_LEFT_CONTROL -> KeyCode.LEFT_CONTROL;
            case GLFW_KEY_LEFT_ALT -> KeyCode.LEFT_ALT;
            case GLFW_KEY_LEFT_SUPER -> KeyCode.LEFT_SUPER;
            case GLFW_KEY_RIGHT_SHIFT -> KeyCode.RIGHT_SHIFT;
            case GLFW_KEY_RIGHT_CONTROL -> KeyCode.RIGHT_CONTROL;
            case GLFW_KEY_RIGHT_ALT -> KeyCode.RIGHT_ALT;
            case GLFW_KEY_RIGHT_SUPER -> KeyCode.RIGHT_SUPER;
            case GLFW_KEY_MENU -> KeyCode.MENU;
            default -> KeyCode.UNKNOWN;
        };
    }

    private InputCounter counter;

    public Keyboard() {
        List<String> keys = Arrays.stream(KeyCode.values()).map(Enum::name).toList();
        counter = new InputCounter(keys);
    }

    public int getPressingCount(KeyCode keyCode) {
        return counter.getPressingCount(keyCode.name());
    }

    public Map<KeyCode, Integer> getPressingCounts() {
        var counts = new HashMap<KeyCode, Integer>();
        counter.getPressingCounts().forEach((name, count) -> {
            counts.put(KeyCode.valueOf(name), count);
        });

        return counts;
    }

    public int getReleasingCount(KeyCode keyCode) {
        return counter.getReleasingCount(keyCode.name());
    }

    public Map<KeyCode, Integer> getReleasingCounts() {
        var counts = new HashMap<KeyCode, Integer>();
        counter.getReleasingCounts().forEach((name, count) -> {
            counts.put(KeyCode.valueOf(name), count);
        });

        return counts;
    }

    public void setPressingFlag(int keyCode, boolean pressingFlag) {
        KeyCode eKeyCode = convertKeyCodeToEnum(keyCode);
        counter.setPressingFlag(eKeyCode.name(), pressingFlag);
    }

    public void update() {
        counter.update();
    }
}
