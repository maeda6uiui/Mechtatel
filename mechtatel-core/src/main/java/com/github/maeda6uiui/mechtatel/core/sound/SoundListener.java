package com.github.maeda6uiui.mechtatel.core.sound;

import org.joml.Vector3fc;

import static org.lwjgl.openal.AL10.*;

/**
 * Sound listener
 *
 * @author maeda6uiui
 */
public class SoundListener {
    public static void setPosition(Vector3fc position) {
        alListener3f(AL_POSITION, position.x(), position.y(), position.z());
    }

    public static void setVelocity(Vector3fc velocity) {
        alListener3f(AL_VELOCITY, velocity.x(), velocity.y(), velocity.z());
    }

    public static void setOrientation(Vector3fc at, Vector3fc up) {
        var data = new float[6];
        data[0] = at.x();
        data[1] = at.y();
        data[2] = at.z();
        data[3] = up.x();
        data[4] = up.y();
        data[5] = up.z();
        alListenerfv(AL_ORIENTATION, data);
    }

    public static void setParameter(int param, float v1, float v2, float v3) {
        alListener3f(param, v1, v2, v3);
    }

    public static void setParameter(int param, float[] vs) {
        alListenerfv(param, vs);
    }

    public static void setParameter(int param, float v) {
        alListenerf(param, v);
    }

    public static void setParameter(int param, int v) {
        alListeneri(param, v);
    }
}
