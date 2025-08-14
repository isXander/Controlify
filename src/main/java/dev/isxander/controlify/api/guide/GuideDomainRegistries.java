package dev.isxander.controlify.api.guide;

public interface GuideDomainRegistries {
    GuideDomainRegistry<InGameCtx> inGame();

    GuideDomainRegistry<ContainerCtx> container();
}
