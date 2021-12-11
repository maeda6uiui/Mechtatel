package com.github.maeda6uiui.mechtatel.core.util;

/**
 * Returns a unique number starting from 0.
 * The number is augmented every time get() is called.
 *
 * @author maeda
 */
public class UniversalCounter {
    private static int count;

    static {
        count = 0;
    }

    private UniversalCounter() {

    }

    public static synchronized int get() {
        int ret = count;
        count++;

        return ret;
    }

    public static synchronized void reset() {
        count = 0;
    }
}
