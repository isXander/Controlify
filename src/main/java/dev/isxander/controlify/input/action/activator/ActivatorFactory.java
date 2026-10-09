package dev.isxander.controlify.input.action.activator;

import dev.isxander.controlify.input.action.Activator;
import dev.isxander.controlify.input.logical.LogicalInput;

public interface ActivatorFactory<T extends Activator> {
	T create(LogicalInput boundInput);
}
