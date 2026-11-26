/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.useitem;

import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin extends LivingEntityMixin {
	@Override
	protected void onStartUsingItem(InteractionHand hand, CallbackInfo ci, ItemStack stack) {
		holdUseItemFeedback(stack, 0);
	}

	@Override
	protected void onUpdateUsingItem(ItemStack stack, CallbackInfo ci) {
		holdUseItemFeedback(stack, ((LocalPlayer) (Object) this).getTicksUsingItem());
	}

	@Override
	protected void onStopUsingItem(CallbackInfo ci) {
		BuiltinFeedback.USE_ITEM.release();
	}

	/// Which effect plays depends on selection rules over the `use_animation/*` facts of the in-game domain.
	@Unique private void holdUseItemFeedback(ItemStack stack, int ticksUsed) {
		float chargeProgress = 0f;
		if (stack.getUseAnimation() == ItemUseAnimation.CROSSBOW) {
			int chargeDuration = CrossbowItem.getChargeDuration(stack, (LocalPlayer) (Object) this);
			// builds while charging and stops once the crossbow is ready
			if (chargeDuration > 0 && ticksUsed < chargeDuration) {
				chargeProgress = (float) ticksUsed / chargeDuration;
			}
		}

		BuiltinFeedback.USE_ITEM.hold(new BuiltinFeedback.ItemUse(chargeProgress));
	}
}
