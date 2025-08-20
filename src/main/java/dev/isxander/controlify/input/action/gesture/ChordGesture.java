package dev.isxander.controlify.input.action.gesture;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.input.action.Accumulator;
import dev.isxander.controlify.input.action.ChannelKind;
import dev.isxander.controlify.input.signal.Signal;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A gesture that requires all of its member latch gestures to be
 * activated within a certain time window.
 * <p>
 * This gesture supports {@link ChannelKind#LATCH}, {@link ChannelKind#PULSE}.
 * <p>
 * It fires a pulse depending on the specified {@link PulsePolicy}.
 * And enables the latch channel when all members are down,
 * and disables it when any member is released.
 */
public class ChordGesture implements SerializableGesture<ChordGesture> {
    private final List<Gesture> members;
    private final Map<Gesture, MemberAccumulator> accumulators;
    private final long allLatchWindowNs;
    private final PulsePolicy pulsePolicy;

    private long lastSignalTime, firstDown;
    private int downCount;
    private boolean hasAllBeenDown, ranOutOfTime;
    private Accumulator target;

    public ChordGesture(List<Gesture> members, long allLatchWindowNs, PulsePolicy pulsePolicy) {
        if (members.isEmpty()) {
            throw new IllegalArgumentException("ChordGesture must have at least one member");
        }
        if (members.stream().anyMatch(g -> !g.supports(ChannelKind.LATCH))) {
            throw new IllegalArgumentException("All members of ChordGesture must support LATCH channel");
        }

        this.members = Collections.unmodifiableList(members);
        this.accumulators = members.stream()
                .collect(Collectors.toMap(g -> g, g -> new MemberAccumulator()));
        this.allLatchWindowNs = allLatchWindowNs;
        this.pulsePolicy = pulsePolicy;
    }

    @Override
    public boolean supports(ChannelKind channel) {
        return channel.isPulse() || channel.isLatch();
    }

    @Override
    public void onSignal(Signal signal, Accumulator acc) {
        this.lastSignalTime = signal.timeNanos();
        this.target = acc;
        this.members.forEach(m -> m.onSignal(signal, this.accumulators.get(m)));
    }

    @Override
    public String describe() {
        return "Chord[" + this.members.stream()
                .map(Gesture::describe)
                .collect(Collectors.joining(", ")) + "]";
    }

    @Override
    public Set<ResourceLocation> monitoredInputs() {
        return this.members.stream()
                .flatMap(g -> g.monitoredInputs().stream())
                .collect(Collectors.toSet());
    }

    private List<Gesture> members() {
        return this.members;
    }

    private long allLatchWindowNs() {
        return this.allLatchWindowNs;
    }

    private PulsePolicy pulsePolicy() {
        return this.pulsePolicy;
    }

    private class MemberAccumulator implements Accumulator {
        private boolean latchActive = false;
        private boolean targetLatchActive = false;

        @Override
        public void setLatch(boolean active) {
            if (active == this.latchActive) return;
            this.latchActive = active;

            if (active) {
                // if this is the first member to press,
                // we mark this time and reset the ranOutOfTime flag
                if (downCount == 0) {
                    firstDown = lastSignalTime;
                    ranOutOfTime = false;
                }

                // always accurately track the down count
                downCount++;

                if (ranOutOfTime || firstDown + allLatchWindowNs >= lastSignalTime) {
                    // if we've run out of time, mark this as a failure
                    // and don't update the target latch
                    ranOutOfTime = true;
                } else if (downCount == members.size()) {
                    // if this is the last member to press, mark the latch
                    if (!hasAllBeenDown && !targetLatchActive) {
                        hasAllBeenDown = true;
                        targetLatchActive = true;
                        target.setLatch(true);

                        if (pulsePolicy == PulsePolicy.ON_DOWN) {
                            target.firePulse();
                        }
                    }
                }
            } else {
                // always accurately track the down count
                downCount--;

                if (targetLatchActive && downCount < members.size()) {
                    targetLatchActive = false;
                    target.setLatch(false);
                }

                if (downCount == 0) {
                    firstDown = 0;
                    if (hasAllBeenDown && pulsePolicy == PulsePolicy.ON_RELEASE) {
                        target.firePulse();
                    }
                    hasAllBeenDown = false;
                }
            }
        }

        @Override
        public void toggleLatch() {
            this.setLatch(!this.latchActive);
        }
    }

    public enum PulsePolicy implements StringRepresentable {
        ON_DOWN, ON_RELEASE;

        @Override
        public @NotNull String getSerializedName() {
            return this.name().toLowerCase();
        }

        public static final Codec<PulsePolicy> CODEC = StringRepresentable.fromEnum(PulsePolicy::values);
    }

    public static final String GESTURE_ID = "chord";
    public static final MapCodec<ChordGesture> MAP_CODEC = RecordCodecBuilder.<ChordGesture>create(instance -> instance.group(
            Gesture.CODEC.listOf().fieldOf("members").forGetter(ChordGesture::members),
            Codec.LONG.optionalFieldOf("all_latch_window_ns", 320L * 1_000L).forGetter(ChordGesture::allLatchWindowNs),
            PulsePolicy.CODEC.optionalFieldOf("pulse_policy", PulsePolicy.ON_DOWN).forGetter(ChordGesture::pulsePolicy)
    ).apply(instance, ChordGesture::new))
            .fieldOf(GESTURE_ID);

    @Override
    public GestureType<ChordGesture> type() {
        return GestureType.CHORD;
    }
}

