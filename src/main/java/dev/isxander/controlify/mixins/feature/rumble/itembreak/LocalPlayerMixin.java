/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.itembreak;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LocalPlayer.class)
public class LocalPlayerMixin extends LivingEntityMixin {
	@Override
	protected void onBreakItemParticles(ItemStack stack, CallbackInfo ci) {
		ControlifyApi.get().playRumbleEffect(
				CUtil.rl("item_break"),
				RumbleEffect.finite().byTick(tick -> new RumbleState(tick <= 4 ? 1f : 0f, 1f), 10)
		);
	}
}
