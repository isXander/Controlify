package dev.isxander.controlify.action;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

public interface ActionSpecBuilder {
	ActionSpecBuilder id(Identifier id);

	ActionSpecBuilder name(Component name);

	ActionSpecBuilder description(Component description);

	ActionSpecBuilder category(Component category);

	ActionSpecBuilder context(Identifier... contexts);

	ActionSpecBuilder shouldExpose(Predicate<ControllerEntity> predicate);

	ActionSpec build();
}
