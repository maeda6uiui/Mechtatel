package com.github.maeda6uiui.mechtatel.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class TestAudioMethods {
    private MttAudio sound;

    @BeforeEach
    public void loadMttAudio() {
        assertDoesNotThrow(() -> {
            sound = new MttAudio("../Data/Op23.mp3");
        });
    }

    @Test
    public void testFileNotFound() {
        assertThrows(FileNotFoundException.class, () -> {
            sound = new MttAudio("example.mp3");
        });
    }

    @Test
    public void testGetSpeed() {
        assertEquals(1.0f, sound.getSpeed());
        sound.setSpeed(1.5f);
        assertEquals(1.5f, sound.getSpeed());
    }

    @Test
    public void testGetVolume() {
        assertEquals(1.0f, sound.getVolume());
        sound.setVolume(1.5f);
        assertEquals(1.5f, sound.getVolume());
    }

    @Test
    public void testGetPos() {
        assertEquals(0, sound.getPos());
    }

    @Test
    public void testCallMethodAfterStop() {
        sound.stop();
        assertThrows(RuntimeException.class, () -> {
            sound.isFinished();
        });
    }
}
