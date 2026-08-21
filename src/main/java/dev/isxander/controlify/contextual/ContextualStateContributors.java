package dev.isxander.controlify.contextual;

import dev.isxander.controlify.api.contextual.*;
import dev.isxander.controlify.mixins.feature.guide.ingame.PlayerAccessor;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BundleItem;
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
	private static final Identifier SLOT_ACTIVE_ITEM = CUtil.rl("active_item");
	private static final Identifier FACT_MAIN_HAND_IS_BLOCK_ITEM = CUtil.rl("main_hand_is_block_item");
	private static final Identifier FACT_OFF_HAND_IS_BLOCK_ITEM = CUtil.rl("off_hand_is_block_item");
	private static final Identifier FACT_FISHING_HOOK_CAST = CUtil.rl("fishing_hook_cast");
	private static final Identifier FACT_ON_CLIMBABLE = CUtil.rl("on_climbable");

	private static final Identifier SLOT_HOVERING_SLOT = CUtil.rl("hovering_slot");
	private static final Identifier SLOT_HOLDING_ITEM = CUtil.rl("holding_item");
	private static final Identifier SLOT_BUNDLE_SELECTED = CUtil.rl("bundle_selected");

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

	public static final ContextualStateContributor<InGameContext> IN_GAME = (context, sink) -> {
		switch (context.hitResult()) {
			case BlockHitResult hitResult when hitResult.getType() == HitResult.Type.BLOCK  -> {
				sink.contributeBlock(
						SLOT_HIT_RESULT,
						new BlockInWorld(context.level(), hitResult.getBlockPos(), false)
				);
			}
			case EntityHitResult hitResult -> {
				sink.contributeEntity(
						SLOT_HIT_RESULT,
						hitResult.getEntity()
				);
			}
			default -> {}
		}

		sink.contributeEntity(SLOT_SELF, context.player());

		sink.contributeItem(SLOT_MAIN_HAND, context.player().getMainHandItem());
		sink.contributeItem(SLOT_OFF_HAND, context.player().getOffhandItem());
		sink.contributeItem(SLOT_ACTIVE_ITEM, context.player().getActiveItem());
		sink.contributeFact(
				FACT_MAIN_HAND_IS_BLOCK_ITEM,
				context.player().getMainHandItem().getItem() instanceof BlockItem
		);
		sink.contributeFact(
				FACT_OFF_HAND_IS_BLOCK_ITEM,
				context.player().getOffhandItem().getItem() instanceof BlockItem
		);
		sink.contributeFact(FACT_FISHING_HOOK_CAST, context.player().fishing != null);
		sink.contributeFact(FACT_ON_CLIMBABLE, context.player().onClimbable());

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
				!context.player().input.getMoveVector().equals(Vec2.ZERO)
		);

		sink.contributeFact(
				CUtil.rl("has_hearts"),
				!context.player().getAbilities().invulnerable
		);
	};

	public static final ContextualStateContributor<ContainerContext> CONTAINER = (context, sink) -> {
		if (context.hoveredSlot() != null) {
			Slot hoveredSlot = context.hoveredSlot();

			sink.contributeItem(
					SLOT_HOVERING_SLOT,
					hoveredSlot.getItem()
			);

			sink.contributeFact(
					CUtil.rl("can_place_held_item"),
					hoveredSlot.mayPlace(context.holdingItem())
			);

			sink.contributeFact(
					CUtil.rl("can_pickup_slot"),
					hoveredSlot.mayPickup(context.player())
			);

			sink.contributeItem(
					SLOT_BUNDLE_SELECTED,
					BundleItem.getSelectedItem(hoveredSlot.getItem())
			);
		}

		sink.contributeItem(
				SLOT_HOLDING_ITEM,
				context.holdingItem()
		);

		sink.contributeFact(
				CUtil.rl("cursor_outside_container"),
				context.cursorOutsideContainer()
		);
	};

	private ContextualStateContributors() {
	}
}
