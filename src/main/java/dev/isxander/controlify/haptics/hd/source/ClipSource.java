package dev.isxander.controlify.haptics.hd.source;

import dev.isxander.controlify.haptics.hd.PcmClip;
import dev.isxander.controlify.haptics.hd.PcmFormat;

import java.nio.FloatBuffer;

/**
 * Source that takes a predefined clip from a buffer and plays it back.
 */
public final class ClipSource implements HDHapticsSource {
    private final PcmClip clip;
    private final int totalSamples;
    private int sampleIndex;

    public ClipSource(PcmClip clip) {
        this.clip = clip;
        this.totalSamples = this.clip.remainingSamples();
        this.sampleIndex = 0;
    }

    @Override
    public boolean mixInto(FloatBuffer out, int frameCount, PcmFormat format) {
        if (format != clip.format()) {
            throw new IllegalStateException("Given clip format does not match bus format.");
        }

        int outSamples = frameCount * format.channels();

        int remaining = totalSamples - sampleIndex;
        int samplesToMix = Math.min(outSamples, remaining);

        int outBase = out.position();
        int clipBase = clip.buffer().position() + sampleIndex;

        for (int i = 0; i < samplesToMix; i++) {
            out.put(outBase + i, out.get(outBase + i) + clip.buffer().get(clipBase + i));
        }

        sampleIndex += samplesToMix;
        return sampleIndex < totalSamples;
    }
}

