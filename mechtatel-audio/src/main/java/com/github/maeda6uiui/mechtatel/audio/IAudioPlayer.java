package com.github.maeda6uiui.mechtatel.audio;

import com.sun.jna.Library;

/**
 * Interface to native audio player
 *
 * @author maeda6uiui
 */
public interface IAudioPlayer extends Library {
    IAudioPlayer INSTANCE = NativeLoader.load();

    String spawn_audio_player_thread(String input_filepath);

    String send_command_to_audio_player(String id, String command);
}
