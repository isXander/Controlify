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
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
//?}

@Mixin(LocalPlayer.class)
public abstract class LocalPlayerMixin implements DropWithResultInvoker {

	//? if >=26.3 {
	// 26.3 release moved LocalPlayer.drop(boolean) to
	// MultiPlayerGameMode.dropItem(LocalPlayer, boolean), which drops the
	// selected stack (and swings) exactly as LocalPlayer.drop used to.
	@Override
	public boolean controlify$drop(boolean all) {
		LocalPlayer self = (LocalPlayer) (Object) this;
		MultiPlayerGameMode gameMode = Minecraft.getInstance().gameMode;
		if (gameMode == null) {
			return false;
		}

		// dropItem() drops inventory.removeFromSelected(all); it is a no-op
		// when the selected slot is empty, which is what the result reports.
		boolean dropped = !self.getInventory().getSelectedItem().isEmpty();
		gameMode.dropItem(self, all);
		return dropped;
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
