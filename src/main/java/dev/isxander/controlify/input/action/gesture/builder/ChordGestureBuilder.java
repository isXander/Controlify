package dev.isxander.controlify.input.action.gesture.builder;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.input.action.gesture.ChordGesture;
import dev.isxander.controlify.input.action.gesture.Gesture;

import java.util.List;
import java.util.Optional;

public record ChordGestureBuilder(
        Optional<List<GestureBuilder<?, ?>>> members,
        Optional<Long> allLatchWindowNs,
        Optional<ChordGesture.PulsePolicy> pulsePolicy
) implements GestureBuilder<ChordGesture, ChordGestureBuilder> {
    @Override
    public ChordGesture build() throws IncompleteBuildException {
        if (members.isEmpty() || members.get().isEmpty()) {
            throw new IllegalStateException("ChordGesture requires at least one member gesture");
        }

        return new ChordGesture(
                members.orElseThrow(() -> new IncompleteBuildException("'members' not specified")).stream().map(b -> (Gesture) b.build()).toList(),
                allLatchWindowNs.orElse(0L), // TODO
                pulsePolicy.orElse(ChordGesture.PulsePolicy.ON_DOWN)
        );
    }

    @Override
    public Optional<ChordGestureBuilder> merge(GestureBuilder<?, ?> other) throws IncompatibleMergeException {
        if (other instanceof ChordGestureBuilder(var otherMembers, var otherAllLatchWindowNs, var otherPulsePolicy)) {
            var mergedMembers = otherMembers.isPresent() ? otherMembers : this.members();
            var mergedAllLatchWindowNs = otherAllLatchWindowNs.isPresent() ? otherAllLatchWindowNs : this.allLatchWindowNs();
            var mergedPulsePolicy = otherPulsePolicy.isPresent() ? otherPulsePolicy : this.pulsePolicy();

            return Optional.of(new ChordGestureBuilder(mergedMembers, mergedAllLatchWindowNs, mergedPulsePolicy));
        }
        return Optional.empty();
    }

    @Override
    public ChordGestureBuilder delta(GestureBuilder<?, ?> other) {
        if (!(other instanceof ChordGestureBuilder cg)) return this;
        return new ChordGestureBuilder(
                this.members().equals(cg.members()) ? Optional.empty() : this.members(),
                this.allLatchWindowNs().equals(cg.allLatchWindowNs()) ? Optional.empty() : this.allLatchWindowNs(),
                this.pulsePolicy().equals(cg.pulsePolicy()) ? Optional.empty() : this.pulsePolicy()
        );
    }

    @Override
    public GestureBuilderType<ChordGestureBuilder> type() {
        return GestureBuilderType.CHORD;
    }

    public static final String GESTURE_ID = "chord";
    public static final MapCodec<ChordGestureBuilder> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            GestureBuilder.CODEC.listOf().optionalFieldOf("members").forGetter(ChordGestureBuilder::members),
            Codec.LONG.optionalFieldOf("all_latch_window_ns").forGetter(ChordGestureBuilder::allLatchWindowNs),
            ChordGesture.PulsePolicy.CODEC.optionalFieldOf("pulse_policy").forGetter(ChordGestureBuilder::pulsePolicy)
    ).apply(instance, ChordGestureBuilder::new));
    public static final Codec<ChordGestureBuilder> CODEC = MAP_CODEC.codec();
}
