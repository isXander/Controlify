package dev.isxander.controlify.impl.action;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.api.action.ActionBinding;
import dev.isxander.controlify.api.action.Gesture;
import dev.isxander.controlify.api.input.LogicalInput;
import dev.isxander.controlify.impl.utils.codec.CExtraCodecs;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Map;
import java.util.Set;

/// All action-related codecs whose classes would be in the api package are here
/// so their CODECs do not become a part of the api.
public final class ActionCodecs {

	private static final Codec<LogicalInput.Single> LOGICAL_INPUT_SINGLE =
			Identifier.CODEC.xmap(LogicalInput.Single::new, LogicalInput.Single::physicalInput);

	private static final Codec<LogicalInput.ModifiedChord> LOGICAL_INPUT_MODIFIED_CHORD =
			RecordCodecBuilder.<Map.Entry<Set<Identifier>, Identifier>>create(instance -> instance.group(
					CExtraCodecs.set(Identifier.CODEC).fieldOf("modifiers").forGetter(Map.Entry::getKey),
					Identifier.CODEC.fieldOf("trigger").forGetter(Map.Entry::getValue)
			).apply(instance, Map::entry)).flatXmap(
					entry -> LogicalInput.ModifiedChord.validate(entry.getKey(), entry.getValue())
							.<DataResult<LogicalInput.ModifiedChord>>map(error -> DataResult.error(() -> error))
							.orElseGet(() -> DataResult.success(new LogicalInput.ModifiedChord(entry.getKey(), entry.getValue()))),
					chord -> DataResult.success(Map.entry(chord.modifiers(), chord.trigger()))
			);

	public static final Codec<LogicalInput> LOGICAL_INPUT =
			CExtraCodecs.fuzzy(
					List.of(LOGICAL_INPUT_SINGLE, LOGICAL_INPUT_MODIFIED_CHORD),
					input -> switch (input) {
						case LogicalInput.Single _ -> LOGICAL_INPUT_SINGLE;
						case LogicalInput.ModifiedChord _ -> LOGICAL_INPUT_MODIFIED_CHORD;
					}
			);


	private static final Codec<ActionBinding> BINDING_RECORD = RecordCodecBuilder.create(instance -> instance.group(
			ActionCodecs.LOGICAL_INPUT.fieldOf("input").forGetter(ActionBinding::input),
			Gesture.CODEC.optionalFieldOf("gesture").forGetter(ActionBinding::gesture)
	).apply(instance, ActionBinding::new));

	/// A bare input, or `{"input": ..., "gesture": ...}`.
	public static final Codec<ActionBinding> BINDING = Codec.either(ActionCodecs.LOGICAL_INPUT, BINDING_RECORD).xmap(
			either -> either.map(ActionBinding::of, binding -> binding),
			binding -> binding.gesture().isEmpty() ? Either.left(binding.input()) : Either.right(binding)
	);

	private ActionCodecs() {
	}
}
