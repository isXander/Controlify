package dev.isxander.controlify.input;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.utils.codec.CExtraCodecs;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Set;

/// A reference to a logical button or axis.
///
/// A logical input's domain is the type of input, meaning that
/// a logical input that refers to the same physical inputs can be
/// safely duplicated between continuous and digital signals.
///
/// Most of the time this will refer to a single physical button or axis,
/// like the `A` button or `Left Stick` left, but it may also refer to
/// a chord, like `A` + `B`.
public sealed interface LogicalInput {
	Codec<LogicalInput> CODEC = CExtraCodecs.fuzzy(
			List.of(Single.CODEC, ModifiedChord.CODEC),
			input -> switch (input) {
				case Single _ -> Single.CODEC;
				case ModifiedChord _ -> ModifiedChord.CODEC;
			}
	);

	/// A logical input with a direct relationship with a physical input.
	record Single(Identifier physicalInput) implements LogicalInput {
		public static final Codec<Single> CODEC =
				Identifier.CODEC.xmap(Single::new, Single::physicalInput);
	}

	/// A logical input that triggers only after all modifiers are already pressed.
	record ModifiedChord(Set<Identifier> modifiers, Identifier trigger) implements LogicalInput {
		public static final Codec<ModifiedChord> CODEC = RecordCodecBuilder.create(instance -> instance.group(
				CExtraCodecs.set(Identifier.CODEC).fieldOf("modifiers").forGetter(ModifiedChord::modifiers),
				Identifier.CODEC.fieldOf("trigger").forGetter(ModifiedChord::trigger)
		).apply(instance, ModifiedChord::new));
	}
}
