package dev.isxander.controlify.haptics.hd;

import java.nio.FloatBuffer;

public record PcmClip(FloatBuffer buffer, PcmFormat format) {
    public PcmClip(FloatBuffer buffer, PcmFormat format) {
        this.buffer = buffer.slice();
        this.format = format;
    }

    public int remainingSamples() {
        return this.buffer.remaining();
    }
}
