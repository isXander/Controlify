package dev.isxander.controlify.driver.sdl;

import com.mojang.blaze3d.audio.OggAudioStream;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

/**
 * Dualsense haptics work on channels 3 and 4 of the regular
 * audio stream of the output device. It is operated just like a speaker.
 */
public class DualsenseOggAudioStream extends OggAudioStream {

    public DualsenseOggAudioStream(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    @Override
    protected void convertMono(FloatBuffer buf, OutputConcat channels) {
        while (buf.hasRemaining()) {
            channels.put(0f); // channel 1
            channels.put(0f); // channel 2
            float sample = buf.get();
            channels.put(sample); // channel 3
            channels.put(sample); // channel 4
        }
    }

    @Override
    protected void convertStereo(FloatBuffer leftBuf, FloatBuffer rightBuf, OutputConcat channels) {
        while (leftBuf.hasRemaining() && rightBuf.hasRemaining()) {
            channels.put(0f); // channel 1
            channels.put(0f); // channel 2
            channels.put(leftBuf.get()); // channel 3
            channels.put(rightBuf.get()); // channel 4
        }
    }

    @Override
    public @NotNull AudioFormat getFormat() {
        AudioFormat format = super.getFormat();
        return new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), 4, true, format.isBigEndian());
    }
}
