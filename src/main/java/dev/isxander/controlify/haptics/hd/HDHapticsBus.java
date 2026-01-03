package dev.isxander.controlify.haptics.hd;

import dev.isxander.controlify.haptics.hd.source.HDHapticsSource;
import org.lwjgl.system.MemoryUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public final class HDHapticsBus implements AutoCloseable {
    private final PcmFormat format;
    private final int ringBufferFrames;
    private final int chunkFrames;

    private final ByteBuffer ringByteBuffer;
    private final FloatBuffer ringFloatBuffer;
    private final DirectFloatRingBuffer ringBuffer;

    private final ByteBuffer chunkByteBuffer;
    private final FloatBuffer chunkFloatBuffer;

    private final HDHapticsMixer mixer;

    public HDHapticsBus(PcmFormat format, int ringBufferFrames, int chunkFrames) {
        this.format = format;
        this.ringBufferFrames = ringBufferFrames;
        this.chunkFrames = chunkFrames;

        int ringSamples = ringBufferFrames * format.channels();
        this.ringByteBuffer = MemoryUtil.memAlloc(ringSamples * 4).order(ByteOrder.LITTLE_ENDIAN);
        this.ringFloatBuffer = ringByteBuffer.asFloatBuffer();
        this.ringBuffer = new DirectFloatRingBuffer(ringFloatBuffer);

        int chunkSamples = chunkFrames * format.channels();
        this.chunkByteBuffer = MemoryUtil.memAlloc(chunkSamples * 4).order(ByteOrder.LITTLE_ENDIAN);
        this.chunkFloatBuffer = chunkByteBuffer.asFloatBuffer();

        this.mixer = new HDHapticsMixer();
        this.mixer.add(HDHapticsSource.keepAlive());
    }

    public PcmFormat format() {
        return format;
    }

    public int ringBufferFrames() {
        return ringBufferFrames;
    }

    public int chunkFrames() {
        return chunkFrames;
    }

    public DirectFloatRingBuffer ringBuffer() {
        return ringBuffer;
    }

    public HDHapticsMixer mixer() {
        return mixer;
    }

    public FloatBuffer chunkFloatBufferView() {
        return chunkFloatBuffer;
    }

    public void ensureBufferedFrames(int targetFrames, int chunkFrames) {
        int channels = format.channels();
        int targetSamples = targetFrames * channels;

        while (ringBuffer.availableSamples() < targetSamples) {
            int freeFrames = ringBuffer.freeSamples() / channels;
            if (freeFrames <= 0) {
                break;
            }

            int framesToRender = Math.min(chunkFrames, freeFrames);
            int samplesToRender = framesToRender * channels;

            chunkFloatBuffer.clear();
            chunkFloatBuffer.limit(samplesToRender);

            mixer.mixInto(chunkFloatBuffer, framesToRender, format);

            chunkFloatBuffer.position(0);
            ringBuffer.writeFrom(chunkFloatBuffer, samplesToRender);
        }
    }

    @Override
    public void close() {
        MemoryUtil.memFree(chunkByteBuffer);
        MemoryUtil.memFree(ringByteBuffer);
    }
}

