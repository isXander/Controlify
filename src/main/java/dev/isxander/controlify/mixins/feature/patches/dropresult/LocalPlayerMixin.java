/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.patches.dropresult;

import dev.isxander.controlify.ingame.DropWithResultInvoker;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

//? if >=26.3 {
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
//?}

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements DropWithResultInvoker {

	//? if >=26.3 {
	@Unique private final ThreadLocal<Boolean> controlify$dropResult = ThreadLocal.withInitial(() -> false);

	@Shadow
	public abstract void drop(boolean all);

	@Inject(method = "drop", at = @At("RETURN"))
	private void setResultField(boolean all, CallbackInfo ci, @Local(name = "prediction") ItemStack prediction) {
		controlify$dropResult.set(!prediction.isEmpty());
	}

	@Override
	public boolean controlify$drop(boolean all) {
		this.drop(all);
		return controlify$dropResult.get();
	}
	//?} else {
	/*@Shadow
	public abstract boolean drop(boolean all);

	@Override
	public boolean controlify$drop(boolean all) {
		return this.drop(all);
	}
	*///?}
}
