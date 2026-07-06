package dev.isxander.controlify.gui.guide;

import dev.isxander.controlify.api.guide.ContainerCtx;
import dev.isxander.controlify.api.guide.ContainerFacts;
import dev.isxander.controlify.api.guide.InGameCtx;
import dev.isxander.controlify.api.guide.InGameFacts;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.HashMap;
import java.util.Map;

public final class GuideDomains {
    private GuideDomains() {}

    public static final GuideDomainImpl<InGameCtx> IN_GAME = new GuideDomainImpl<>(CUtil.rl("in_game"));
    public static final GuideDomainImpl<ContainerCtx> CONTAINER = new GuideDomainImpl<>(CUtil.rl("container"));

    public static final Map<Identifier, GuideDomainImpl<?>> CUSTOM_DOMAINS = new HashMap<>();

    public static void freeze() {
        IN_GAME.freeze();
        CONTAINER.freeze();
        CUSTOM_DOMAINS.values().forEach(GuideDomainImpl::freeze);
    }

    static {
        InGameFacts.registerAll();
        ContainerFacts.registerAll();
    }
}
