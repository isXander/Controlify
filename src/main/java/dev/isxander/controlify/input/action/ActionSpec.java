package dev.isxander.controlify.input.action;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.Set;

public record ActionSpec(
		Identifier id,
		Component name,
		Component description,
		Component category,
		Set<Identifier> contexts
) {
}
