package com.github.maeda6uiui.mechtatel.core.sound;

import org.joml.Vector3fc;

/**
 * Provides interface to sound source
 *
 * @author maeda6uiui
 */
public interface ISoundSource {
    void setParameter(int param, float v1, float v2, float v3);

    void setParameter(int param, float v);

    void setParameter(int param, int v);

    void setPosition(Vector3fc position);

    void play();

    boolean isPlaying();

    void pause();

    void stop();
}
