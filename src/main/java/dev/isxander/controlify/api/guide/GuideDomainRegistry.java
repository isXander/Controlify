package dev.isxander.controlify.api.guide;

import net.minecraft.resources.Identifier;

public interface GuideDomainRegistry {
    GuideDomain<InGameCtx> inGame();

    GuideDomain<ContainerCtx> container();

    <T extends FactCtx> GuideDomain<T> registerCustom(Identifier domainId);
}
