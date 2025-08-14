package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.controller.ControllerEntity;

public interface FactCtx {
    ControllerEntity controller();

    GuideVerbosity verbosity();
}
