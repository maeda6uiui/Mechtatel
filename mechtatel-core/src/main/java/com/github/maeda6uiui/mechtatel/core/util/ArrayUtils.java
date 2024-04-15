package com.github.maeda6uiui.mechtatel.core.util;

import java.util.List;

/**
 * Utility methods for arrays
 *
 * @author maeda6uiui
 */
public class ArrayUtils {
    public static float[] listFloatToArray(List<Float> lst) {
        int size = lst != null ? lst.size() : 0;
        float[] floatArr = new float[size];
        for (int i = 0; i < size; i++) {
            floatArr[i] = lst.get(i);
        }

        return floatArr;
    }

    public static int[] listIntToArray(List<Integer> lst) {
        return lst.stream().mapToInt(Integer::intValue).toArray();
    }
}
