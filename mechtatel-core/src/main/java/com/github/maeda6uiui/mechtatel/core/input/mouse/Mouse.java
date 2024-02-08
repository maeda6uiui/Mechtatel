package com.github.maeda6uiui.mechtatel.core.input.mouse;

import com.github.maeda6uiui.mechtatel.core.input.InputCounter;

import java.util.Arrays;
import java.util.List;

import static org.lwjgl.glfw.GLFW.*;

/**
 * Mouse
 *
 * @author maeda6uiui
 */
public class Mouse {
    private static MouseCode convertMouseCodeToEnum(int mouseCode) {
        return switch (mouseCode) {
            case GLFW_MOUSE_BUTTON_1 -> MouseCode.LEFT;
            case GLFW_MOUSE_BUTTON_2 -> MouseCode.RIGHT;
            case GLFW_MOUSE_BUTTON_3 -> MouseCode.MIDDLE;
            case GLFW_MOUSE_BUTTON_4 -> MouseCode.BUTTON_4;
            case GLFW_MOUSE_BUTTON_5 -> MouseCode.BUTTON_5;
            case GLFW_MOUSE_BUTTON_6 -> MouseCode.BUTTON_6;
            case GLFW_MOUSE_BUTTON_7 -> MouseCode.BUTTON_7;
            case GLFW_MOUSE_BUTTON_8 -> MouseCode.BUTTON_8;
            default -> MouseCode.UNKNOWN;
        };
    }

    private InputCounter counter;
    private double x;
    private double y;

    public Mouse() {
        List<String> keys = Arrays.stream(MouseCode.values()).map(Enum::name).toList();
        counter = new InputCounter(keys);
    }

    public int getPressingCount(MouseCode mouseCode) {
        return counter.getPressingCount(mouseCode.name());
    }

    public int getReleasingCount(MouseCode mouseCode) {
        return counter.getReleasingCount(mouseCode.name());
    }

    public void setPressingFlag(int mouseCode, boolean pressingFlag) {
        MouseCode eMouseCode = convertMouseCodeToEnum(mouseCode);
        counter.setPressingFlag(eMouseCode.name(), pressingFlag);
    }

    public void setCursorPos(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getCursorPosX() {
        return x;
    }

    public double getCursorPosY() {
        return y;
    }

    public void update() {
        counter.update();
    }
}
