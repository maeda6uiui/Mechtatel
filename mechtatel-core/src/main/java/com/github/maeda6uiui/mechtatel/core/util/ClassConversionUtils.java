package com.github.maeda6uiui.mechtatel.core.util;

import com.jme3.math.Matrix3f;
import org.joml.*;

import java.awt.*;

/**
 * Utility methods for class conversion
 *
 * @author maeda6uiui
 */
public class ClassConversionUtils {
    public static Vector3f convertJMEVector3fToJOMLVector3f(com.jme3.math.Vector3f src) {
        var ret = new Vector3f(src.x, src.y, src.z);
        return ret;
    }

    public static com.jme3.math.Vector3f convertJOMLVector3fToJMEVector3f(Vector3fc src) {
        var ret = new com.jme3.math.Vector3f(src.x(), src.y(), src.z());
        return ret;
    }

    public static Matrix4f convertJMEMatrix3fToJOMLMatrix4f(Matrix3f src) {
        var ret = new Matrix4f().identity();

        ret.m00(src.get(0, 0));
        ret.m01(src.get(1, 0));
        ret.m02(src.get(2, 0));
        ret.m10(src.get(0, 1));
        ret.m11(src.get(1, 1));
        ret.m12(src.get(2, 1));
        ret.m20(src.get(0, 2));
        ret.m21(src.get(1, 2));
        ret.m22(src.get(2, 2));

        return ret;
    }

    public static Matrix3f convertJOMLMatrix4fToJMEMatrix3f(Matrix4fc src) {
        var ret = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ret.set(i, j, src.get(j, i));
            }
        }

        return ret;
    }

    public static Vector4f convertJavaColorToJOMLVector4f(Color color) {
        float r = color.getRed() / 255.0f;
        float g = color.getGreen() / 255.0f;
        float b = color.getBlue() / 255.0f;

        var ret = new Vector4f(r, g, b, 1.0f);

        return ret;
    }
}
