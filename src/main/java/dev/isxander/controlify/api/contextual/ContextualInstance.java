package dev.isxander.controlify.api.contextual;

public interface ContextualInstance<C extends Context> {
	void update(C context);
}
