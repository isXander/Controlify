package dev.isxander.controlify.haptics.hd.source;

import dev.isxander.controlify.haptics.hd.PcmFormat;

import java.nio.FloatBuffer;

/**
 * A source of HD haptics waveform data.
 * A source must mix their waveform data into an existing buffer.
 */
public interface HDHapticsSource {
    /**
     * Mixes this source into an output interleaved PCM buffer.
     * @param out the interleaved pcm buffer to mix into
     * @param frameCount requested amount of frames to mix into the output buffer
     * @param format the bus format that the buffer is in
     * @return true if this source is alive
     */
    boolean mixInto(FloatBuffer out, int frameCount, PcmFormat format);

    default CancellableHapticsSource cancellable() {
        return new CancellableHapticsSource(this);
    }

    static HDHapticsSource keepAlive() {
        return (out, frameCount, format) -> true;
    }
}

