package com.github.maeda6uiui.mechtatel.core.sound;

import org.joml.Vector3fc;

import java.io.IOException;
import java.net.URI;

import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_POSITION;

/**
 * 3D sound
 * 3D functionalities are supposed to work only when you load a monaural sound.
 *
 * @author maeda6uiui
 */
public class MttSound {
    private SoundBuffer buffer;
    private SoundSource source;
    private boolean isDuplicatedSound;
    private boolean isCleanedUp;

    public MttSound(URI soundResource, boolean loop, boolean relative) throws IOException {
        buffer = new SoundBuffer(soundResource);
        source = new SoundSource(buffer, loop, relative);
        isDuplicatedSound = false;
        isCleanedUp = false;
    }

    public MttSound(MttSound srcSound, boolean loop, boolean relative) {
        buffer = srcSound.buffer;
        source = new SoundSource(buffer, loop, relative);
        isDuplicatedSound = true;
        isCleanedUp = false;
    }

    public void cleanup() {
        if (!isCleanedUp) {
            if (!isDuplicatedSound) {
                buffer.cleanup();
            }
            source.cleanup();

            isCleanedUp = true;
        }
    }

    public void setPosition(Vector3fc position) {
        source.setParameter(AL_POSITION, position.x(), position.y(), position.z());
    }

    public void setGain(float gain) {
        source.setParameter(AL_GAIN, gain);
    }

    public void play() {
        source.play();
    }

    public boolean isPlaying() {
        return source.isPlaying();
    }

    public void pause() {
        source.pause();
    }

    public void stop() {
        source.stop();
    }
}
