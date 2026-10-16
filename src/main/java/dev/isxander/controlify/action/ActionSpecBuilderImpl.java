package dev.isxander.controlify.action;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.apache.commons.lang3.Validate;
import org.jspecify.annotations.Nullable;

import java.util.*;
import java.util.function.Predicate;

public class ActionSpecBuilderImpl implements ActionSpecBuilder {
	private @Nullable Identifier id;
	private @Nullable Component name;
	private @Nullable Component description;
	private @Nullable Component category;
	private final Set<Identifier> contexts = new TreeSet<>();
	private Predicate<ControllerEntity> shouldExpose = _ -> true;

	@Override
	public ActionSpecBuilder id(Identifier id) {
		this.id = id;
		return this;
	}

	@Override
	public ActionSpecBuilder name(Component name) {
		this.name = name;
		return this;
	}

	@Override
	public ActionSpecBuilder description(Component description) {
		this.description = description;
		return this;
	}

	@Override
	public ActionSpecBuilder category(Component category) {
		this.category = category;
		return this;
	}

	@Override
	public ActionSpecBuilder context(Identifier... contexts) {
		this.contexts.addAll(List.of(contexts));
		return this;
	}

	@Override
	public ActionSpecBuilder shouldExpose(Predicate<ControllerEntity> predicate) {
		this.shouldExpose = predicate;
		return this;
	}

	@Override
	public ActionSpec build() {
		Validate.notNull(this.id, "An action spec needs an id");
		Validate.notNull(this.category, "An action spec needs a category");
		Validate.isTrue(!this.contexts.isEmpty(), "An action spec needs at least one context");

		Component name = Optional.ofNullable(this.name)
				.orElseGet(() -> Component.translatable(this.id.toLanguageKey("controlify.action")));
		Component description = Optional.ofNullable(this.description)
				.orElseGet(() -> Component.translatable(this.id.toLanguageKey("controlify.action", "desc")));

		return new ActionSpec(
				this.id,
				name,
				description,
				this.category,
				Collections.unmodifiableSet(this.contexts),
				this.shouldExpose
		);
	}
}
