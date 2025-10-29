package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.input.action.gesture.LatchRepeatPulseGesture;

import java.util.Optional;

public record LatchRepeatPulseGestureBuilder(
        Optional<GestureBuilder<?, ?>> latchGesture,
        Optional<Long> initialDelayNs,
        Optional<Long> repeatDelayNs
) implements GestureBuilder<LatchRepeatPulseGesture, LatchRepeatPulseGestureBuilder> {
    @Override
    public LatchRepeatPulseGesture build() {
        return new LatchRepeatPulseGesture(
                this.latchGesture().orElseThrow().build(),
                this.initialDelayNs().orElseThrow(),
                this.repeatDelayNs().orElseThrow()
        );
    }

    @Override
    public Optional<LatchRepeatPulseGestureBuilder> merge(GestureBuilder<?, ?> other) throws IncompatibleMergeException {
        if (other instanceof LatchRepeatPulseGestureBuilder(Optional<GestureBuilder<?, ?>> otherLatchGesture, Optional<Long> otherInitialDelayNs, Optional<Long> otherRepeatDelayNs)) {
            Optional<GestureBuilder<?, ?>> thisLatchGesture = this.latchGesture();
            Optional<Long> thisInitialDelayNs = this.initialDelayNs();
            Optional<Long> thisRepeatDelayNs = this.repeatDelayNs();
            return Optional.of(new LatchRepeatPulseGestureBuilder(
                    otherLatchGesture.or(() -> thisLatchGesture),
                    otherInitialDelayNs.or(() -> thisInitialDelayNs),
                    otherRepeatDelayNs.or(() -> thisRepeatDelayNs)
            ));
        }
        return Optional.empty();
    }

    @Override
    public LatchRepeatPulseGestureBuilder delta(GestureBuilder<?, ?> other) {
        if (!(other instanceof LatchRepeatPulseGestureBuilder lrpg)) return this;
        return new LatchRepeatPulseGestureBuilder(
                this.latchGesture().equals(lrpg.latchGesture()) ? Optional.empty() : this.latchGesture(),
                this.initialDelayNs().equals(lrpg.initialDelayNs()) ? Optional.empty() : this.initialDelayNs(),
                this.repeatDelayNs().equals(lrpg.repeatDelayNs()) ? Optional.empty() : this.repeatDelayNs()
        );
    }

    @Override
    public GestureBuilderType<LatchRepeatPulseGestureBuilder> type() {
        return GestureBuilderType.LATCH_REPEAT_PULSE;
    }

    public static final String GESTURE_ID = "latch_repeat_pulse";
    public static final MapCodec<LatchRepeatPulseGestureBuilder> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GestureBuilder.CODEC.optionalFieldOf("latch").forGetter(LatchRepeatPulseGestureBuilder::latchGesture),
            Codec.LONG.optionalFieldOf("initial_delay").forGetter(LatchRepeatPulseGestureBuilder::initialDelayNs),
            Codec.LONG.optionalFieldOf("repeat_delay").forGetter(LatchRepeatPulseGestureBuilder::repeatDelayNs)
    ).apply(instance, LatchRepeatPulseGestureBuilder::new));
    public static final Codec<LatchRepeatPulseGestureBuilder> CODEC = MAP_CODEC.codec();
}
