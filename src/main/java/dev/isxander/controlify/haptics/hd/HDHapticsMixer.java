package dev.isxander.controlify.haptics.hd;

import dev.isxander.controlify.haptics.hd.source.HDHapticsSource;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Mixes multiple sources into a single source.
 * This source remains alive until all of its mixed sources are dead.
 */
public final class HDHapticsMixer implements HDHapticsSource {
    private final List<HDHapticsSource> sources = new ArrayList<>();

    public void add(HDHapticsSource source) {
        sources.add(source);
    }

    public void clear() {
        sources.clear();
    }

    @Override
    public boolean mixInto(FloatBuffer out, int frameCount, PcmFormat format) {
        int sampleCount = frameCount * format.channels();
        int base = out.position();

        // make sure the buffer is initialized completely
        for (int i = 0; i < sampleCount; i++) {
            out.put(base + i, 0.0f);
        }

        // allow each source to mix
        Iterator<HDHapticsSource> iterator = sources.iterator();
        while (iterator.hasNext()) {
            HDHapticsSource source = iterator.next();
            boolean alive = source.mixInto(out, frameCount, format);
            if (!alive) {
                iterator.remove();
            }
        }

        // prevent any samples being out of bounds [-1,1]
        for (int i = 0; i < sampleCount; i++) {
            float value = out.get(base + i);
            if (value > 1.0f) {
                value = 1.0f;
            } else if (value < -1.0f) {
                value = -1.0f;
            }
            out.put(base + i, value);
        }

        return !sources.isEmpty();
    }
}

