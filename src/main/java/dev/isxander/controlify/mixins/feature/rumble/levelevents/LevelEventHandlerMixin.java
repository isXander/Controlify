/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.levelevents;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.Easings;
import net.minecraft.client.renderer.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.LevelEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LevelEventHandler.class)
public class LevelEventHandlerMixin {
	@Inject(method = "levelEvent", at = @At("HEAD"))
	private void onLevelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
		switch (eventId) {
			case LevelEvent.SOUND_ANVIL_USED -> ControlifyApi.get().playRumbleEffect(
					CUtil.rl("anvil"),
					RumbleEffect.finite().constant(1f, 0.5f, 2)
							.then(RumbleEffect.finite().constant(0f, 0f, 5)).repeat(3)
			);
		}
	}

	@Inject(method = "globalLevelEvent", at = @At("HEAD"))
	private void onGlobalLevelEvent(int eventId, BlockPos pos, int data, CallbackInfo ci) {
		switch (eventId) {
			case LevelEvent.SOUND_DRAGON_DEATH -> ControlifyApi.get().playRumbleEffect(
					CUtil.rl("dragon_death"),
					RumbleEffect.finite().constant(1f, 1f, 194)
							.then(RumbleEffect.finite().byTime(t -> {
								float easeOutQuad = (float) Easings.easeOutQuad(t);
								return new RumbleState(1 - easeOutQuad, 1 - easeOutQuad);
							}, 63))
			);
			case LevelEvent.SOUND_WITHER_BOSS_SPAWN -> ControlifyApi.get().playRumbleEffect(
					CUtil.rl("wither_spawn"),
					RumbleEffect.finite().constant(1f, 1f, 9)
							.then(RumbleEffect.finite().constant(0.1f, 1f, 14))
							.then(RumbleEffect.finite().byTime(t -> {
								float easeOutQuad = 1 - (1 - t) * (1 - t);
								return new RumbleState(0f, 1 - easeOutQuad);
							}, 56))
			);
		}
	}
}
