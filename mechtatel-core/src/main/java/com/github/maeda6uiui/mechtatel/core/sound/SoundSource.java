package com.github.maeda6uiui.mechtatel.core.sound;

import org.joml.Vector3fc;

import static org.lwjgl.openal.AL10.*;

/**
 * Sound source
 *
 * @author maeda6uiui
 */
class SoundSource {
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

    public void setPosition(Vector3fc position) {
        alSource3f(source, AL_POSITION, position.x(), position.y(), position.z());
    }

    public void setVelocity(Vector3fc velocity) {
        alSource3f(source, AL_VELOCITY, velocity.x(), velocity.y(), velocity.z());
    }

    public void setGain(float gain) {
        alSourcef(source, AL_GAIN, gain);
    }

    public void play() {
        alSourcePlay(source);
    }

    public boolean isPlaying() {
        return alGetSourcei(source, AL_SOURCE_STATE) == AL_PLAYING;
    }

    public void pause() {
        alSourcePause(source);
    }

    public void stop() {
        alSourceStop(source);
    }

    public void cleanup() {
        this.stop();
        alDeleteSources(source);
    }
}
