package com.github.maeda6uiui.mechtatel.core.sound;

import java.io.IOException;
import java.nio.file.Path;

/**
 * 3D sound<br>
 * 3D functionalities are supposed to work only when you load a monaural sound.
 *
 * @author maeda6uiui
 */
public class MttSound {
    private SoundBuffer buffer;
    private SoundSource source;
    private boolean isDuplicatedSound;
    private boolean isCleanedUp;

    public MttSound(Path soundFile, boolean loop, boolean relative) throws IOException {
        buffer = new SoundBuffer(soundFile);
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

    public ISoundSource getSoundSource() {
        return source;
    }
}
