package dev.isxander.controlify.input.action;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

public interface ActionSpecBuilder {
	ActionSpecBuilder id(Identifier id);

	ActionSpecBuilder name(Component name);

	ActionSpecBuilder description(Component name);

	ActionSpecBuilder category(Component category);

	ActionSpecBuilder context(Identifier... contexts);

	ActionSpec build();

	ActionAccessor buildAndRegister(ActionSpecRegistry registry);
}
