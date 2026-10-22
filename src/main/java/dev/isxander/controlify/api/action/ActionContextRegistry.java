package dev.isxander.controlify.api.action;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;

import java.util.function.Function;

/// Registers context predicates during {@link ActionEvents#REGISTER_CONTEXTS}.
/// Context records remain internal; actions reference their identifiers only.
public interface ActionContextRegistry {
	/// Registers a predicate evaluated against the current client before controller input is processed.
	/// Returns id for use in {@link ActionSpecBuilder#context(Identifier...)}.
	/// Throws if the identifier already exists or context registration has closed.
	@Contract("_, _ -> param1")
	Identifier register(Identifier id, Function<Minecraft, Boolean> isApplicable);
}
