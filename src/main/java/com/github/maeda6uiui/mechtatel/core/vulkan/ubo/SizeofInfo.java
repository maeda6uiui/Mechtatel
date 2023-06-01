package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

/**
 * Sizeof info
 *
 * @author maeda6uiui
 */
public class SizeofInfo {
    public static final int SIZEOF_INT = Integer.BYTES;
    public static final int SIZEOF_FLOAT = Float.BYTES;
    public static final int SIZEOF_VEC2 = Float.BYTES * 2;
    public static final int SIZEOF_VEC3 = Float.BYTES * 3;
    public static final int SIZEOF_VEC4 = Float.BYTES * 4;
    public static final int SIZEOF_MAT4 = SIZEOF_VEC4 * 4;
}
