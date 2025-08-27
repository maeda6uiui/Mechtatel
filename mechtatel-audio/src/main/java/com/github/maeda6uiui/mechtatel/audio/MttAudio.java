package com.github.maeda6uiui.mechtatel.audio;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Audio player
 *
 * @author maeda6uiui
 */
public class MttAudio {
    private String playerId;

    public MttAudio(String filepath) throws FileNotFoundException {
        if (!Files.exists(Paths.get(filepath))) {
            throw new FileNotFoundException(filepath);
        }

        playerId = IAudioPlayer.INSTANCE.spawn_audio_player_thread(filepath);
    }

    private void throwExceptionOnError(String resp) {
        if (resp.startsWith("error_")) {
            throw new RuntimeException(resp);
        }
    }

    public void play() {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, "play");
        this.throwExceptionOnError(resp);
    }

    /**
     * Stops the sound.
     * Note that methods in this class cannot be called after the sound is stopped.
     */
    public void stop() {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, "stop");
        this.throwExceptionOnError(resp);
    }

    /**
     * Pauses playback of this sound.
     * A paused sound can be resumed with {@link #play()}.
     */
    public void pause() {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, "pause");
        this.throwExceptionOnError(resp);
    }

    public boolean isFinished() {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, "is_finished");
        this.throwExceptionOnError(resp);

        return resp.equals("true");
    }

    public boolean isPaused() {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, "is_paused");
        this.throwExceptionOnError(resp);

        return resp.equals("true");
    }

    public float getSpeed() {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, "get_speed");
        this.throwExceptionOnError(resp);

        return Float.parseFloat(resp);
    }

    public float getVolume() {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, "get_volume");
        this.throwExceptionOnError(resp);

        return Float.parseFloat(resp);
    }

    public void setSpeed(float speed) {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, String.format("set_speed %f", speed));
        this.throwExceptionOnError(resp);
    }

    public void setVolume(float volume) {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, String.format("set_volume %f", volume));
        this.throwExceptionOnError(resp);
    }

    /**
     * Returns the position of the sound in millisecond.
     *
     * @return Position of the sound in millisecond
     */
    public int getPos() {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, "get_pos");
        this.throwExceptionOnError(resp);

        return Integer.parseInt(resp);
    }

    /**
     * Attempts to seek to a given position.
     *
     * @param pos Position in millisecond
     */
    public void seek(int pos) {
        String resp = IAudioPlayer.INSTANCE.send_command_to_audio_player(playerId, String.format("seek %d", pos));
        this.throwExceptionOnError(resp);
    }
}
