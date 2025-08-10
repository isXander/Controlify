package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.gui.guide.GuideDomains;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

/**
 * Contains facts that can be used in any guide domain.
 */
public final class CommonFacts {
    private CommonFacts() {}

    /**
     * When the guide verbosity level is set to {@link GuideVerbosity#FULL FULL}.
     * Used in {@link Rule#forbid()} clauses to only allow the rule to be applied when the verbosity
     * is less than full.
     */
    public static final Fact<FactCtx> VERBOSITY_FULL = register(
            CUtil.rl("verbosity_full"),
            ctx -> ctx.verbosity() == GuideVerbosity.FULL
    );
    /**
     * When the guide verbosity level is either {@link GuideVerbosity#FULL FULL} or {@link GuideVerbosity#REDUCED REDUCED}.
     * Used in {@link Rule#forbid()} clauses to only allow the rule to be applied when the verbosity is
     * set to minimal.
     */
    public static final Fact<FactCtx> VERBOSITY_REDUCED_OR_MORE = register(
            CUtil.rl("verbosity_reduced_or_more"),
            ctx -> ctx.verbosity().getLevel() >= GuideVerbosity.REDUCED.getLevel()
    );
    /**
     * When the guide verbosity is set to {@link GuideVerbosity#REDUCED REDUCED} or less.
     * Used in {@link Rule#when()} clauses to only allow the rule to be applied when the verbosity
     * is set to reduced or minimal.
     */
    public static final Fact<FactCtx> VERBOSITY_REDUCED_OR_LESS = register(
            CUtil.rl("verbosity_reduced_or_less"),
            ctx -> ctx.verbosity().getLevel() <= GuideVerbosity.REDUCED.getLevel()
    );
    /**
     * When the guide verbosity is set to {@link GuideVerbosity#MINIMAL MINIMAL}.
     * Used in {@link Rule#when()} clauses to only allow the rule to be applied when the verbosity
     * is set to minimal.
     */
    public static final Fact<FactCtx> VERBOSITY_MINIMAL = register(
            CUtil.rl("verbosity_minimal"),
            ctx -> ctx.verbosity() == GuideVerbosity.MINIMAL
    );

    private static Fact<FactCtx> register(ResourceLocation id, FactProvider<FactCtx> provider) {
        var fact = Fact.of(id, provider);

        GuideDomains.IN_GAME.registerFact(fact);
        GuideDomains.CONTAINER.registerFact(fact);

        return fact;
    }
    private static Fact<FactCtx> register(ResourceLocation id) {
        return register(id, FactProvider.staticProvider(false));
    }

    @ApiStatus.Internal
    public static void registerAll() {
        // This method is used to ensure that all facts are registered
        // when the class is loaded, so that they can be used in the guide.
        // No-op, as all facts are registered statically.
    }
}
