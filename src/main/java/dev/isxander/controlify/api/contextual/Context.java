package dev.isxander.controlify.api.contextual;

import dev.isxander.controlify.controller.ControllerEntity;

public interface Context {
	ControllerEntity controller();

	GuideVerbosity verbosity();
}
