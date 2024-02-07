package com.github.maeda6uiui.mechtatel.core.sound;

import com.github.maeda6uiui.mechtatel.core.util.ByteBufferUtils;
import org.lwjgl.BufferUtils;
import org.lwjgl.stb.STBVorbisInfo;

import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.stb.STBVorbis.*;

/**
 * Sound buffer
 *
 * @author maeda6uiui
 */
class SoundBuffer {
    private int buffer;

    private ShortBuffer readVorbis(URI resource, int bufferSize, STBVorbisInfo info) throws IOException {
        ByteBuffer vorbis = ByteBufferUtils.ioResourceToByteBuffer(resource, bufferSize);
        IntBuffer error = BufferUtils.createIntBuffer(1);
        long decoder = stb_vorbis_open_memory(vorbis, error, null);
        if (decoder == 0) {
            throw new RuntimeException("Failed to open Ogg Vorbis file. Error: " + error.get(0));
        }

        stb_vorbis_get_info(decoder, info);

        int channels = info.channels();
        ShortBuffer pcm = BufferUtils.createShortBuffer(stb_vorbis_stream_length_in_samples(decoder) * channels);

        stb_vorbis_get_samples_short_interleaved(decoder, channels, pcm);
        stb_vorbis_close(decoder);

        return pcm;
    }

    public SoundBuffer(URI soundResource) throws IOException {
        this.buffer = alGenBuffers();
        try (STBVorbisInfo info = STBVorbisInfo.malloc()) {
            ShortBuffer pcm = this.readVorbis(soundResource, 32 * 1024, info);
            alBufferData(buffer, info.channels() == 1 ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16, pcm, info.sample_rate());
        }
    }

    public int getBuffer() {
        return buffer;
    }

    public void cleanup() {
        alDeleteBuffers(buffer);
    }
}
