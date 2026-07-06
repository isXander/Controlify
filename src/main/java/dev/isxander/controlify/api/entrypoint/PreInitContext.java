package dev.isxander.controlify.api.entrypoint;

import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.guide.GuideDomainRegistry;

public interface PreInitContext {
    ControlifyBindApi bindings();

    GuideDomainRegistry guideRegistries();
}
