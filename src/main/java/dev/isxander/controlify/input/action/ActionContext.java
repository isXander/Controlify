package dev.isxander.controlify.input.action;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.function.Function;

public record ActionContext(Identifier id, Function<Minecraft, Boolean> isApplicable) implements Comparable<ActionContext> {
	@Override
	public int compareTo(@NonNull ActionContext o) {
		return this.id().compareTo(o.id());
	}
}
