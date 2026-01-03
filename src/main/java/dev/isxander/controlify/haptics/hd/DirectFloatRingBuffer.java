package dev.isxander.controlify.haptics.hd;

import java.nio.FloatBuffer;

public final class DirectFloatRingBuffer {
    private final FloatBuffer storage;
    private final int capacitySamples;

    private int readIndexSamples;
    private int writeIndexSamples;
    private int availableSamples;

    public DirectFloatRingBuffer(FloatBuffer storage) {
        if (!storage.isDirect()) {
            throw new IllegalArgumentException("storage must be a direct FloatBuffer");
        }

        this.storage = storage;
        this.capacitySamples = storage.capacity();
    }

    public int capacitySamples() {
        return capacitySamples;
    }

    public int availableSamples() {
        return availableSamples;
    }

    public int freeSamples() {
        return capacitySamples - availableSamples;
    }

    public void clear() {
        readIndexSamples = 0;
        writeIndexSamples = 0;
        availableSamples = 0;
    }

    public int writeFrom(FloatBuffer src, int sampleCount) {
        int clamped = Math.min(sampleCount, freeSamples());
        int remaining = clamped;

        while (remaining > 0) {
            int contiguous = Math.min(remaining, capacitySamples - writeIndexSamples);

            int srcOldPos = src.position();
            int srcOldLimit = src.limit();

            storage.position(writeIndexSamples);
            storage.limit(writeIndexSamples + contiguous);

            src.limit(srcOldPos + contiguous);
            storage.put(src);

            src.limit(srcOldLimit);
            src.position(srcOldPos + contiguous);

            writeIndexSamples = (writeIndexSamples + contiguous) % capacitySamples;
            availableSamples += contiguous;
            remaining -= contiguous;
        }

        return clamped;
    }

    public int readInto(FloatBuffer dst, int sampleCount) {
        int clamped = Math.min(sampleCount, availableSamples);
        int remaining = clamped;

        while (remaining > 0) {
            int contiguous = Math.min(remaining, capacitySamples - readIndexSamples);

            int dstOldPos = dst.position();
            int dstOldLimit = dst.limit();

            storage.position(readIndexSamples);
            storage.limit(readIndexSamples + contiguous);

            dst.limit(dstOldPos + contiguous);
            dst.put(storage);

            dst.limit(dstOldLimit);
            dst.position(dstOldPos + contiguous);

            readIndexSamples = (readIndexSamples + contiguous) % capacitySamples;
            availableSamples -= contiguous;
            remaining -= contiguous;
        }

        return clamped;
    }
}
