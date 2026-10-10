package dev.isxander.controlify.input.action;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.Comparator;
import java.util.Set;

public record ActionSpec(
		Identifier id,
		Component name,
		Component description,
		Component category,
		Set<Identifier> contexts
) implements Comparable<ActionSpec> {
	@Override
	public int compareTo(@NonNull ActionSpec o) {
		return Comparator.comparing(ActionSpec::id)
				.compare(this, o);
	}
}
