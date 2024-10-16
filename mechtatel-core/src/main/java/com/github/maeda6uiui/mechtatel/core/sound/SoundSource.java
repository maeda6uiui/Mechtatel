package com.github.maeda6uiui.mechtatel.core.sound;

import org.joml.Vector3fc;

import static org.lwjgl.openal.AL10.*;

/**
 * Sound source
 *
 * @author maeda6uiui
 */
class SoundSource implements ISoundSource {
    private int source;

    public SoundSource(SoundBuffer buffer, boolean loop, boolean relative) {
        source = alGenSources();
        alSourcei(source, AL_BUFFER, buffer.getBuffer());

        if (loop) {
            alSourcei(source, AL_LOOPING, AL_TRUE);
        }
        if (relative) {
            alSourcei(source, AL_SOURCE_RELATIVE, AL_TRUE);
        }
    }

    @Override
    public void setParameter(int param, float v1, float v2, float v3) {
        alSource3f(source, param, v1, v2, v3);
    }

    @Override
    public void setParameter(int param, float[] vs) {
        alSourcefv(source, param, vs);
    }

    @Override
    public void setParameter(int param, float v) {
        alSourcef(source, param, v);
    }

    @Override
    public void setParameter(int param, int v) {
        alSourcei(source, param, v);
    }

    @Override
    public void setPosition(Vector3fc position) {
        this.setParameter(AL_POSITION, position.x(), position.y(), position.z());
    }

    @Override
    public void play() {
        alSourcePlay(source);
    }

    @Override
    public boolean isPlaying() {
        return alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING;
    }

    @Override
    public void pause() {
        alSourcePause(source);
    }

    @Override
    public void stop() {
        alSourceStop(source);
    }

    public void cleanup() {
        this.stop();
        alDeleteSources(source);
    }
}
