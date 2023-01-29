package com.github.maeda6uiui.mechtatel.core.vulkan.ubo;

/**
 * Sizeof info
 *
 * @author maeda6uiui
 */
public class SizeofInfo {
    public static final int SIZEOF_INT = Integer.BYTES;
    public static final int SIZEOF_FLOAT = Float.BYTES;
    public static final int SIZEOF_VEC2 = 2 * Float.BYTES;
    public static final int SIZEOF_VEC3 = 3 * Float.BYTES;
    public static final int SIZEOF_VEC4 = 4 * Float.BYTES;
    public static final int SIZEOF_MAT4 = 4 * SIZEOF_VEC4;
}
