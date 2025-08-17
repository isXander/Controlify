package dev.isxander.controlify.input.action.gesture;

import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

/**
 * A gesture that uses a latch gesture to emit repeating pulses.
 * <p>
 * This gesture supports {@link ChannelKind#PULSE}.
 */
public class LatchRepeatPulseGesture implements Gesture {
    private final Gesture latchGesture;
    private final long initialDelayNs, repeatDelayNs;
    private final HoldRepeatAccumulator holdRepeatAccumulator;

    private boolean down;
    private long nextDue;

    public LatchRepeatPulseGesture(Gesture latchGesture, long initialDelayNs, long repeatDelayNs) {
        if (!latchGesture.supports(ChannelKind.LATCH)) {
            throw new IllegalArgumentException("Latch gesture is required for HoldRepeatGesture");
        }
        this.latchGesture = latchGesture;
        this.initialDelayNs = initialDelayNs;
        this.repeatDelayNs = repeatDelayNs;
        this.holdRepeatAccumulator = new HoldRepeatAccumulator();
    }

    @Override
    public boolean supports(ChannelKind channel) {
        return channel.isPulse();
    }

    @Override
    public void onSignal(Signal signal, Accumulator acc) {
        this.holdRepeatAccumulator.target = acc;
        this.holdRepeatAccumulator.time = signal.timeNanos();

        this.latchGesture.onSignal(signal, this.holdRepeatAccumulator);

        if (signal instanceof Signal.Tick) {
            if (down && signal.timeNanos() >= nextDue) {
                acc.firePulse();
                nextDue += repeatDelayNs;
            }
        }
    }

    @Override
    public String describe() {
        return "HoldRepeat[" + latchGesture.describe() + ", initialDelay=" + initialDelayNs + "ns, repeatDelay=" + repeatDelayNs + "ns]";
    }

    @Override
    public Set<ResourceLocation> monitoredInputs() {
        return latchGesture.monitoredInputs();
    }

    public static LatchRepeatPulseGesture ofHoldNs(ResourceLocation input, long initialDelayNs, long repeatDelayNs) {
        return new LatchRepeatPulseGesture(new HoldGesture(input), initialDelayNs, repeatDelayNs);
    }

    public static LatchRepeatPulseGesture ofHoldTicks(ResourceLocation input, int initialDelayTicks, int repeatDelayTicks) {
        return new LatchRepeatPulseGesture(new HoldGesture(input), initialDelayTicks * 50_000_000L, repeatDelayTicks * 50_000_000L);
    }

    private class HoldRepeatAccumulator implements Accumulator {
        private Accumulator target;
        private long time;

        @Override
        public void setLatch(boolean active) {
            if (active) {
                down = true;
                nextDue = time + initialDelayNs;
                target.firePulse();
            } else {
                down = false;
                nextDue = Long.MAX_VALUE;
            }
        }

        @Override
        public void toggleLatch() {
            this.setLatch(!down);
        }
    }
}
