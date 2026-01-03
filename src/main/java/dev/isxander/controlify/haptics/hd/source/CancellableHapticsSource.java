package dev.isxander.controlify.haptics.hd.source;

import dev.isxander.controlify.haptics.hd.PcmFormat;

import java.nio.FloatBuffer;

public class CancellableHapticsSource implements HDHapticsSource {
    private final HDHapticsSource source;
    private boolean cancelled = false;

    public CancellableHapticsSource(HDHapticsSource source) {
        this.source = source;
    }

    @Override
    public boolean mixInto(FloatBuffer out, int frameCount, PcmFormat format) {
        if (cancelled) {
            return false;
        }
        return source.mixInto(out, frameCount, format);
    }

    /**
     * Cancels this source.
     * The source will not stop until all the queued buffer is consumed by the source.
     * It only prevents further data being mixed into the stream.
     */
    public void cancel() {
        cancelled = true;
    }
}
