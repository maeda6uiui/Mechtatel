package com.github.maeda6uiui.mechtatel.audio;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class TestAudioMethods {
    private MttAudio audio;

    @BeforeEach
    public void loadMttAudio() {
        assertDoesNotThrow(() -> {
            audio = new MttAudio("../Mechtatel/Standard/Audio/op_8.mp3");
        });
    }

    @Test
    public void testFileNotFound() {
        assertThrows(FileNotFoundException.class, () -> {
            audio = new MttAudio("example.mp3");
        });
    }

    @Test
    public void testGetSpeed() {
        assertEquals(1.0f, audio.getSpeed());
        audio.setSpeed(1.5f);
        assertEquals(1.5f, audio.getSpeed());
    }

    @Test
    public void testGetVolume() {
        assertEquals(1.0f, audio.getVolume());
        audio.setVolume(1.5f);
        assertEquals(1.5f, audio.getVolume());
    }

    @Test
    public void testGetPos() {
        assertEquals(0, audio.getPos());
    }

    @Test
    public void testCallMethodAfterStop() {
        audio.stop();
        assertThrows(RuntimeException.class, () -> {
            audio.isFinished();
        });
    }
}
