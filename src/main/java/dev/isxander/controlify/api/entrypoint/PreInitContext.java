package dev.isxander.controlify.api.entrypoint;

import dev.isxander.controlify.api.guide.GuideDomainRegistries;

public interface PreInitContext {
    GuideDomainRegistries guideRegistries();
}
