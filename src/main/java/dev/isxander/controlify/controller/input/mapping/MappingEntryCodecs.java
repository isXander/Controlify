package dev.isxander.controlify.controller.input.mapping;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.controller.input.HatState;
import net.minecraft.resources.Identifier;

final class MappingEntryCodecs {

    private static final Codec<MappingEntryStub> CODEC_STUB = RecordCodecBuilder.create(instance -> instance.group(
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntryStub::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntryStub::outputType)
    ).apply(instance, MappingEntryStub::new));

    public static final Codec<MappingEntry.FromButton.ToButton> CODEC_BUTTON_TO_BUTTON = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromButton.ToButton::from),
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromButton.ToButton::to),
            Codec.BOOL.fieldOf("invert").forGetter(MappingEntry.FromButton.ToButton::invert),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromButton.ToButton::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromButton.ToButton::outputType)
    ).apply(instance, MappingEntry.FromButton.ToButton::new));

    public static final Codec<MappingEntry.FromButton.ToAxis> CODEC_BUTTON_TO_AXIS = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromButton.ToAxis::from),
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromButton.ToAxis::to),
            Codec.FLOAT.fieldOf("off_state").forGetter(MappingEntry.FromButton.ToAxis::offState),
            Codec.FLOAT.fieldOf("on_state").forGetter(MappingEntry.FromButton.ToAxis::onState),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromButton.ToAxis::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromButton.ToAxis::outputType)
    ).apply(instance, MappingEntry.FromButton.ToAxis::new));

    public static final Codec<MappingEntry.FromButton.ToHat> CODEC_BUTTON_TO_HAT = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromButton.ToHat::from),
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromButton.ToHat::to),
            HatState.CODEC.fieldOf("off_state").forGetter(MappingEntry.FromButton.ToHat::offState),
            HatState.CODEC.fieldOf("on_state").forGetter(MappingEntry.FromButton.ToHat::onState),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromButton.ToHat::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromButton.ToHat::outputType)
    ).apply(instance, MappingEntry.FromButton.ToHat::new));

    public static final Codec<MappingEntry.FromAxis.ToButton> CODEC_AXIS_TO_BUTTON = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromAxis.ToButton::from),
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromAxis.ToButton::to),
            Codec.FLOAT.fieldOf("threshold").forGetter(MappingEntry.FromAxis.ToButton::threshold),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromAxis.ToButton::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromAxis.ToButton::outputType)
    ).apply(instance, MappingEntry.FromAxis.ToButton::new));

    public static final Codec<MappingEntry.FromAxis.ToAxis> CODEC_AXIS_TO_AXIS = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromAxis.ToAxis::from),
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromAxis.ToAxis::to),
            Codec.FLOAT.fieldOf("min_in").forGetter(MappingEntry.FromAxis.ToAxis::minIn),
            Codec.FLOAT.fieldOf("min_out").forGetter(MappingEntry.FromAxis.ToAxis::minOut),
            Codec.FLOAT.fieldOf("max_in").forGetter(MappingEntry.FromAxis.ToAxis::maxIn),
            Codec.FLOAT.fieldOf("max_out").forGetter(MappingEntry.FromAxis.ToAxis::maxOut),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromAxis.ToAxis::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromAxis.ToAxis::outputType)
    ).apply(instance, MappingEntry.FromAxis.ToAxis::new));

    public static final Codec<MappingEntry.FromAxis.ToHat> CODEC_AXIS_TO_HAT = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromAxis.ToHat::from),
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromAxis.ToHat::to),
            Codec.FLOAT.fieldOf("threshold").forGetter(MappingEntry.FromAxis.ToHat::threshold),
            HatState.CODEC.fieldOf("target_state").forGetter(MappingEntry.FromAxis.ToHat::targetState),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromAxis.ToHat::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromAxis.ToHat::outputType)
    ).apply(instance, MappingEntry.FromAxis.ToHat::new));

    public static final Codec<MappingEntry.FromHat.ToButton> CODEC_HAT_TO_BUTTON = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromHat.ToButton::from),
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromHat.ToButton::to),
            HatState.CODEC.fieldOf("target_state").forGetter(MappingEntry.FromHat.ToButton::targetState),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromHat.ToButton::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromHat.ToButton::outputType)
    ).apply(instance, MappingEntry.FromHat.ToButton::new));

    public static final Codec<MappingEntry.FromHat.ToAxis> CODEC_HAT_TO_AXIS = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromHat.ToAxis::from),
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromHat.ToAxis::to),
            HatState.CODEC.fieldOf("target_state").forGetter(MappingEntry.FromHat.ToAxis::targetState),
            Codec.FLOAT.fieldOf("on_state").forGetter(MappingEntry.FromHat.ToAxis::onState),
            Codec.FLOAT.fieldOf("off_state").forGetter(MappingEntry.FromHat.ToAxis::offState),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromHat.ToAxis::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromHat.ToAxis::outputType)
    ).apply(instance, MappingEntry.FromHat.ToAxis::new));

    public static final Codec<MappingEntry.FromHat.ToHat> CODEC_HAT_TO_HAT = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("from").forGetter(MappingEntry.FromHat.ToHat::from),
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromHat.ToHat::to),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromHat.ToHat::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromHat.ToHat::outputType)
    ).apply(instance, MappingEntry.FromHat.ToHat::new));

    public static final Codec<MappingEntry.FromNothing.ToButton> CODEC_NOTHING_TO_BUTTON = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromNothing.ToButton::to),
            Codec.BOOL.fieldOf("state").forGetter(MappingEntry.FromNothing.ToButton::state),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromNothing.ToButton::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromNothing.ToButton::outputType)
    ).apply(instance, MappingEntry.FromNothing.ToButton::new));

    public static final Codec<MappingEntry.FromNothing.ToAxis> CODEC_NOTHING_TO_AXIS = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromNothing.ToAxis::to),
            Codec.FLOAT.fieldOf("state").forGetter(MappingEntry.FromNothing.ToAxis::state),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromNothing.ToAxis::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromNothing.ToAxis::outputType)
    ).apply(instance, MappingEntry.FromNothing.ToAxis::new));

    public static final Codec<MappingEntry.FromNothing.ToHat> CODEC_NOTHING_TO_HAT = RecordCodecBuilder.create(instance -> instance.group(
            Identifier.CODEC.fieldOf("to").forGetter(MappingEntry.FromNothing.ToHat::to),
            MapType.CODEC.fieldOf("input_type").forGetter(MappingEntry.FromNothing.ToHat::inputType),
            MapType.CODEC.fieldOf("output_type").forGetter(MappingEntry.FromNothing.ToHat::outputType)
    ).apply(instance, MappingEntry.FromNothing.ToHat::new));

    public static final Codec<MappingEntry> CODEC = new Codec<>() {
        @Override
        public <T> DataResult<T> encode(MappingEntry input, DynamicOps<T> ops, T prefix) {
            return switch (input) {
                case MappingEntry.FromButton.ToButton e -> CODEC_BUTTON_TO_BUTTON.encode(e, ops, prefix);
                case MappingEntry.FromButton.ToAxis e -> CODEC_BUTTON_TO_AXIS.encode(e, ops, prefix);
                case MappingEntry.FromButton.ToHat e -> CODEC_BUTTON_TO_HAT.encode(e, ops, prefix);
                case MappingEntry.FromAxis.ToButton e -> CODEC_AXIS_TO_BUTTON.encode(e, ops, prefix);
                case MappingEntry.FromAxis.ToAxis e -> CODEC_AXIS_TO_AXIS.encode(e, ops, prefix);
                case MappingEntry.FromAxis.ToHat e -> CODEC_AXIS_TO_HAT.encode(e, ops, prefix);
                case MappingEntry.FromHat.ToButton e -> CODEC_HAT_TO_BUTTON.encode(e, ops, prefix);
                case MappingEntry.FromHat.ToAxis e -> CODEC_HAT_TO_AXIS.encode(e, ops, prefix);
                case MappingEntry.FromHat.ToHat e -> CODEC_HAT_TO_HAT.encode(e, ops, prefix);
                case MappingEntry.FromNothing.ToButton e -> CODEC_NOTHING_TO_BUTTON.encode(e, ops, prefix);
                case MappingEntry.FromNothing.ToAxis e -> CODEC_NOTHING_TO_AXIS.encode(e, ops, prefix);
                case MappingEntry.FromNothing.ToHat e -> CODEC_NOTHING_TO_HAT.encode(e, ops, prefix);
            };
        }

        @Override
        public <T> DataResult<Pair<MappingEntry, T>> decode(DynamicOps<T> ops, T input) {
            // First decode the stub to determine types
            return CODEC_STUB.parse(ops, input).flatMap(stub -> switch (stub) {
                case MappingEntryStub s when s.inputType() == MapType.BUTTON && s.outputType() == MapType.BUTTON ->
                        CODEC_BUTTON_TO_BUTTON.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.BUTTON && s.outputType() == MapType.AXIS ->
                        CODEC_BUTTON_TO_AXIS.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.BUTTON && s.outputType() == MapType.HAT ->
                        CODEC_BUTTON_TO_HAT.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.AXIS && s.outputType() == MapType.BUTTON ->
                        CODEC_AXIS_TO_BUTTON.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.AXIS && s.outputType() == MapType.AXIS ->
                        CODEC_AXIS_TO_AXIS.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.AXIS && s.outputType() == MapType.HAT ->
                        CODEC_AXIS_TO_HAT.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.HAT && s.outputType() == MapType.BUTTON ->
                        CODEC_HAT_TO_BUTTON.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.HAT && s.outputType() == MapType.AXIS ->
                        CODEC_HAT_TO_AXIS.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.HAT && s.outputType() == MapType.HAT ->
                        CODEC_HAT_TO_HAT.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.NOTHING && s.outputType() == MapType.BUTTON ->
                        CODEC_NOTHING_TO_BUTTON.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.NOTHING && s.outputType() == MapType.AXIS ->
                        CODEC_NOTHING_TO_AXIS.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                case MappingEntryStub s when s.inputType() == MapType.NOTHING && s.outputType() == MapType.HAT ->
                        CODEC_NOTHING_TO_HAT.decode(ops, input)
                                .map(pair -> pair.mapFirst(e -> (MappingEntry) e));
                default -> DataResult.error(() -> "Unknown MappingEntry type for input type " + stub.inputType() + " and output type " + stub.outputType());
            });
        }
    };


    private record MappingEntryStub(MapType inputType, MapType outputType) {
    }

    private MappingEntryCodecs() {
    }

}
