package com.github.maeda6uiui.mechtatel.audio;

import com.sun.jna.Library;
import com.sun.jna.Pointer;

/**
 * Interface to native audio player
 *
 * @author maeda6uiui
 */
public interface IAudioPlayer extends Library {
    IAudioPlayer INSTANCE = NativeLoader.load();

    Pointer spawn_audio_player_thread(String input_filepath);

    Pointer send_command_to_audio_player(String id, String command);

    void free_str(Pointer p);
}
