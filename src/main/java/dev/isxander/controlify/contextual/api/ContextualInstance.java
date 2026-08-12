package dev.isxander.controlify.contextual.api;

public interface ContextualInstance<C extends Context> {
	void update(C context);
}
