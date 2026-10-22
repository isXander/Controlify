/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.useitem;

import dev.isxander.controlify.impl.Controlify;

import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleEffectInstance;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.controller.rumble.effects.UseItemEffectHolder;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends LivingEntityMixin implements UseItemEffectHolder {
	@Unique private RumbleEffectInstance useItemRumble;

	@Override
	protected void onStartUsingItem(InteractionHand hand, CallbackInfo ci, ItemStack stack) {
		switch (stack.getUseAnimation()) {
			case BOW -> startRumble(RumbleEffect.continuous()
					.byTick(tick -> new RumbleState(
							tick % 7 <= 3 && tick > BowItem.MAX_DRAW_DURATION ? 0.1f : 0f,
							BowItem.getPowerForTime(tick)
					)));
			case CROSSBOW -> {
				int chargeDuration = CrossbowItem.getChargeDuration(
						stack,
						(LocalPlayer) (Object) this
				);
				startRumble(RumbleEffect.continuous()
						.byTick(tick -> new RumbleState(
								0f,
								(float) tick / chargeDuration
						))
						.timeout(chargeDuration));
			}
			case BLOCK, SPYGLASS -> startRumble(RumbleEffect.continuous()
					.byTick(tick -> new RumbleState(
							0f,
							tick % 4 / 4f * 0.12f + 0.05f
					)));
			case EAT, DRINK -> startRumble(RumbleEffect.continuous()
					.constant(0.1f, 0.2f));
			case TOOT_HORN -> startRumble(RumbleEffect.continuous()
					.byTick(tick -> new RumbleState(
							Math.min(1f, tick / 10f),
							0.25f
					)));
			case SPEAR -> startRumble(RumbleEffect.continuous()
					.constant(0.3f, 0.3f));
		}
	}

	@Override
	protected void onUpdateUsingItem(ItemStack stack, CallbackInfo ci) {

	}

	@Override
	protected void onStopUsingItem(CallbackInfo ci) {
		if (useItemRumble != null) {
			useItemRumble.stop();
			useItemRumble = null;
		}
	}

	@Unique private void startRumble(RumbleEffect effect) {
		if (useItemRumble != null) useItemRumble.stop();
		this.useItemRumble = Controlify.instance().playRumbleEffect(CUtil.rl("use_item"), effect).orElse(null);
	}

	@Override
	public @Nullable RumbleEffectInstance controlify$getUseItemEffect() {
		return useItemRumble;
	}
}
