package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.gui.guide.GuideDomain;
import org.jetbrains.annotations.ApiStatus;

public interface GuideDomainRegistries {
    GuideDomainRegistry<InGameCtx> inGame();

    GuideDomainRegistry<ContainerCtx> container();

    @ApiStatus.Internal
    <T extends FactCtx> RenderableGuideDomain<T> registerCustom(GuideDomain<T> domain);
}
