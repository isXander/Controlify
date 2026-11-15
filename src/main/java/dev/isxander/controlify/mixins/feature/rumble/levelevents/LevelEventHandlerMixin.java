/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.rumble.levelevents;

import dev.isxander.controlify.impl.feedback.BuiltinFeedback;
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
	private void onLevelEvent(int eventType, BlockPos pos, int data, CallbackInfo ci) {
		switch (eventType) {
			case LevelEvent.SOUND_ANVIL_USED -> BuiltinFeedback.ANVIL.fire();
		}
	}

	@Inject(method = "globalLevelEvent", at = @At("HEAD"))
	private void onGlobalLevelEvent(int type, BlockPos pos, int data, CallbackInfo ci) {
		switch (type) {
			case LevelEvent.SOUND_DRAGON_DEATH -> BuiltinFeedback.DRAGON_DEATH.fire();
			case LevelEvent.SOUND_WITHER_BOSS_SPAWN -> BuiltinFeedback.WITHER_SPAWN.fire();
		}
	}
}
