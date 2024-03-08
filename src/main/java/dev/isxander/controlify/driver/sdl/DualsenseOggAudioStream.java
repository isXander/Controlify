package dev.isxander.controlify.driver.sdl;

import com.mojang.blaze3d.audio.OggAudioStream;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.nio.FloatBuffer;

public class DualsenseOggAudioStream extends OggAudioStream {

    public DualsenseOggAudioStream(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    @Override
    protected void convertMono(FloatBuffer buf, OutputConcat channels) {
        while (buf.hasRemaining()) {
            channels.put(0f);
            channels.put(0f);
            float sample = buf.get();
            channels.put(sample);
            channels.put(sample);
        }
    }

    @Override
    protected void convertStereo(FloatBuffer leftBuf, FloatBuffer rightBuf, OutputConcat channels) {
        while (leftBuf.hasRemaining() && rightBuf.hasRemaining()) {
            channels.put(0f);
            channels.put(0f);
            channels.put(leftBuf.get());
            channels.put(rightBuf.get());
        }
    }

    @Override
    public @NotNull AudioFormat getFormat() {
        AudioFormat format = super.getFormat();
        return new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), 4, true, format.isBigEndian());
    }
}
