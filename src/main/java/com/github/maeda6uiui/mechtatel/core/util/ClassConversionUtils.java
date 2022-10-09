package com.github.maeda6uiui.mechtatel.core.util;

import com.jme3.math.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Vector3f;
import org.joml.Vector3fc;

/**
 * Utility methods for class conversion
 *
 * @author maeda
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
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ret.set(i, j, src.get(i, j));
            }
        }

        return ret;
    }

    public static Matrix3f convertJOMLMatrix4fToJMEMatrix3f(Matrix4fc src) {
        var ret = new Matrix3f();
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                ret.set(i, j, src.get(i, j));
            }
        }

        return ret;
    }
}
