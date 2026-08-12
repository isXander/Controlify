package dev.isxander.controlify.contextual;

import dev.isxander.controlify.api.guide.GuideVerbosity;
import dev.isxander.controlify.api.guide.InGameCtx;
import dev.isxander.controlify.contextual.api.Context;
import dev.isxander.controlify.contextual.api.ContextualStateContributor;
import dev.isxander.controlify.mixins.feature.guide.ingame.PlayerAccessor;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;

public final class ContextualStateContributors {
	private static final Identifier SLOT_SELF = CUtil.rl("self");
	private static final Identifier SLOT_HIT_RESULT = CUtil.rl("hit_result");
	private static final Identifier SLOT_MAIN_HAND = CUtil.rl("main_hand");
	private static final Identifier SLOT_OFF_HAND = CUtil.rl("off_hand");
	/// Picks the main hand, but if it is empty picks the offhand
	private static final Identifier SLOT_ACTIVE_ITEM = CUtil.rl("active_item");

	public static final ContextualStateContributor<Context> COMMON = (context, sink) -> {
		sink.contributeFact(
				CUtil.rl("verbosity_full"),
				context.verbosity() == GuideVerbosity.FULL
		);
		sink.contributeFact(
				CUtil.rl("verbosity_reduced"),
				context.verbosity().getLevel() >= GuideVerbosity.REDUCED.getLevel()
		);
		sink.contributeFact(
				CUtil.rl("verbosity_minimal"),
				context.verbosity().getLevel() >= GuideVerbosity.MINIMAL.getLevel()
		);
	};

	public static final ContextualStateContributor<InGameCtx> IN_GAME = (context, sink) -> {
		switch (context.hitResult()) {
			case BlockHitResult hitResult when hitResult.getType() == HitResult.Type.BLOCK  -> {
				sink.contributeBlock(
						SLOT_HIT_RESULT,
						new BlockInWorld(context.level(), hitResult.getBlockPos(), false)
				);
				sink.contributeFact(CUtil.rl("looking_at_block"));
			}
			case EntityHitResult hitResult -> {
				sink.contributeEntity(
						SLOT_HIT_RESULT,
						hitResult.getEntity()
				);
				sink.contributeFact(CUtil.rl("looking_at_entity"));
			}
			default -> {
				sink.contributeFact(CUtil.rl("looking_at_air"));
			}
		}

		sink.contributeEntity(SLOT_SELF, context.player());

		sink.contributeItem(SLOT_MAIN_HAND, context.player().getMainHandItem());
		sink.contributeItem(SLOT_OFF_HAND, context.player().getOffhandItem());
		sink.contributeItem(SLOT_ACTIVE_ITEM, context.player().getActiveItem());

		sink.contributeFact(
				CUtil.rl("can_elytra_fly"),
				((PlayerAccessor) context.player()).controlify$callCanGlide()
						&& !context.player().onClimbable()
						&& !context.player().onGround()
						&& !context.player().isInLiquid()
						&& !context.player().isFallFlying()
		);

		sink.contributeFact(
				CUtil.rl("in_liquid"),
				context.player().isInLiquid()
		);

		sink.contributeFact(
				CUtil.rl("under_water"),
				context.player().isUnderWater()
		);

		sink.contributeFact(
				CUtil.rl("is_toggle_sneak"),
				context.controller().settings().generic.toggleSneak
		);
		sink.contributeFact(
				CUtil.rl("is_toggle_sprint"),
				context.controller().settings().generic.toggleSprint
		);
		sink.contributeFact(
				CUtil.rl("input_moving"),
				context.player().input.getMoveVector().equals(Vec2.ZERO)
		);

		// TODO: add GameTypePredicate to ClientEntityPredicate
		// and make them derived facts

		sink.contributeFact(
				CUtil.rl("has_hearts"),
				!context.player().getAbilities().invulnerable
		);


	};

	private ContextualStateContributors() {
	}
}
