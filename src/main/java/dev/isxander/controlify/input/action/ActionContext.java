package dev.isxander.controlify.input.action;

import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

public record ActionContext(Identifier id) implements Comparable<ActionContext> {
	@Override
	public int compareTo(@NonNull ActionContext o) {
		return this.id().compareTo(o.id());
	}
}
