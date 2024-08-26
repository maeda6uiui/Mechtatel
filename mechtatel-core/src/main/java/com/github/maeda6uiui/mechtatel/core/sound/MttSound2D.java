package com.github.maeda6uiui.mechtatel.core.sound;

import com.goxr3plus.streamplayer.stream.StreamPlayer;
import com.goxr3plus.streamplayer.stream.StreamPlayerEvent;
import com.goxr3plus.streamplayer.stream.StreamPlayerException;
import com.goxr3plus.streamplayer.stream.StreamPlayerListener;

import java.nio.file.Path;
import java.util.Map;

/**
 * 2D sound
 *
 * @author maeda6uiui
 */
public class MttSound2D extends StreamPlayer implements StreamPlayerListener {
    public MttSound2D(Path soundFile) throws StreamPlayerException {
        addStreamPlayerListener(this);
        open(soundFile);
    }

    @Override
    public void opened(Object dataSource, Map<String, Object> properties) {

    }

    @Override
    public void progress(int nEncodeBytes, long microsecondPosition, byte[] pcmData, Map<String, Object> properties) {

    }

    @Override
    public void statusUpdated(StreamPlayerEvent streamPlayerEvent) {

    }
}
