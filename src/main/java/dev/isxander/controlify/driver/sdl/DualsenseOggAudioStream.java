package dev.isxander.controlify.driver.sdl;

import it.unimi.dsi.fastutil.floats.FloatConsumer;
import org.jetbrains.annotations.NotNull;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;

/**
 * DualSense haptics work on channels 3 and 4 of the regular
 * audio stream of the output device. It is operated just like a speaker.
 */
public class DualsenseOggAudioStream extends
        /*? if >1.20.4 {*/
        net.minecraft.client.sounds.JOrbisAudioStream
        /*?} else {*/
        /*com.mojang.blaze3d.audio.OggAudioStream
        *//*?}*/
{

    public DualsenseOggAudioStream(InputStream inputStream) throws IOException {
        super(inputStream);
    }

    public static void convertMono(float[] buf, FloatConsumer sampleConsumer) {
        for (float sample : buf) {
            sampleConsumer.accept(0f);  // channel 1
            sampleConsumer.accept(0f);  // channel 2
            sampleConsumer.accept(sample); // channel 3
            sampleConsumer.accept(sample); // channel 4
        }
    }

    public static void convertStereo(float[] bufLeft, float[] bufRight, FloatConsumer sampleConsumer) {
        for (int i = 0; i < bufLeft.length; i++) {
            sampleConsumer.accept(0f);
            sampleConsumer.accept(0f);
            sampleConsumer.accept(bufLeft[i]);
            sampleConsumer.accept(bufRight[i]);
        }
    }

    /*? if <=1.20.4 {*/
    /*@Override
    protected void convertMono(java.nio.FloatBuffer buf, OutputConcat channels) {
        float[] bufArr = new float[buf.limit()];
        buf.rewind();
        buf.get(bufArr);

        convertMono(bufArr, channels::put);
    }

    @Override
    protected void convertStereo(java.nio.FloatBuffer leftBuf, java.nio.FloatBuffer rightBuf, OutputConcat channels) {
        float[] leftBufArr = new float[leftBuf.limit()];
        leftBuf.rewind();
        leftBuf.get(leftBufArr);
        float[] rightBufArr = new float[rightBuf.limit()];
        rightBuf.rewind();
        rightBuf.get(rightBufArr);

        convertStereo(leftBufArr, rightBufArr, channels::put);
    }
    *//*?}*/

    @Override
    public @NotNull AudioFormat getFormat() {
        AudioFormat format = super.getFormat();
        return new AudioFormat(format.getSampleRate(), format.getSampleSizeInBits(), 4, true, format.isBigEndian());
    }
}
