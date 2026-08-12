package dev.isxander.controlify.contextual.api;

import dev.isxander.controlify.api.guide.GuideVerbosity;
import dev.isxander.controlify.controller.ControllerEntity;

public interface Context {
	ControllerEntity controller();

	GuideVerbosity verbosity();
}
