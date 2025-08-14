package dev.isxander.controlify.gui.guide;

import dev.isxander.controlify.api.guide.ContainerCtx;
import dev.isxander.controlify.api.guide.ContainerFacts;
import dev.isxander.controlify.api.guide.InGameCtx;
import dev.isxander.controlify.api.guide.InGameFacts;
import dev.isxander.controlify.utils.CUtil;

public final class GuideDomains {
    private GuideDomains() {}

    public static final GuideDomain<InGameCtx> IN_GAME = new GuideDomain<>(CUtil.rl("in_game"));
    public static final GuideDomain<ContainerCtx> CONTAINER = new GuideDomain<>(CUtil.rl("container"));

    public static void freeze() {
        IN_GAME.freeze();
        CONTAINER.freeze();
    }

    static {
        InGameFacts.registerAll();
        ContainerFacts.registerAll();
    }
}
