package dev.isxander.controlify.haptics.hd.source;

import dev.isxander.controlify.haptics.hd.PcmFormat;

import java.nio.FloatBuffer;
import java.util.concurrent.ThreadLocalRandom;

public final class ProceduralNoiseSource implements HDHapticsSource {
    private float leftGain;
    private float rightGain;

    private float leftState;
    private float rightState;

    public ProceduralNoiseSource(float leftGain, float rightGain) {
        this.setGains(leftGain, rightGain);
    }

    public ProceduralNoiseSource() {
        this(1, 1);
    }

    public void setGains(float leftGain, float rightGain) {
        this.leftGain = clamp01(leftGain);
        this.rightGain = clamp01(rightGain);
    }

    @Override
    public boolean mixInto(FloatBuffer interleavedOut, int frameCount, PcmFormat format) {
        if (format.channels() != 2) {
            throw new IllegalStateException("ProceduralNoiseSource expects stereo");
        }

        float smoothing = 0.12f;
        ThreadLocalRandom rng = ThreadLocalRandom.current();

        int base = interleavedOut.position();

        for (int frame = 0; frame < frameCount; frame++) {
            float ln = (rng.nextFloat() * 2.0f) - 1.0f;
            float rn = (rng.nextFloat() * 2.0f) - 1.0f;

            leftState += (ln - leftState) * smoothing;
            rightState += (rn - rightState) * smoothing;

            int i = base + (frame * 2);
            interleavedOut.put(i, interleavedOut.get(i) + (leftState * leftGain));
            interleavedOut.put(i + 1, interleavedOut.get(i + 1) + (rightState * rightGain));
        }

        return true;
    }

    private static float clamp01(float value) {
        if (value < 0.0f) {
            return 0.0f;
        }
        if (value > 1.0f) {
            return 1.0f;
        }
        return value;
    }
}

