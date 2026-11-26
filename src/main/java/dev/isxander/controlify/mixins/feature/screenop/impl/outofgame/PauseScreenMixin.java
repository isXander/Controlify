/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import dev.isxander.controlify.impl.screenop.behaviours.vanilla.PauseScreenBehaviour;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

//? if >=26.4 {
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import net.minecraft.client.gui.screens.reporting.DraftIconButton;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
//?} else {
//import org.spongepowered.asm.mixin.Shadow;
//?}

@Mixin(PauseScreen.class)
public class PauseScreenMixin implements PauseScreenBehaviour.Access {
	//? if >=26.4 {
	// 26.4 no longer keeps the disconnect button in a field
	@Unique private @Nullable Button disconnectButton;

	@ModifyExpressionValue(method = "createPauseMenu", at = @At(value = "NEW", target = "Lnet/minecraft/client/gui/screens/reporting/DraftIconButton;"))
	private DraftIconButton captureDisconnectButton(DraftIconButton original) {
		return (DraftIconButton) (this.disconnectButton = original);
	}
	//?} else {
	/*@Shadow
	private @Nullable Button disconnectButton;
	*///?}

	@Override
	public @Nullable Button controlify$getDisconnectButton() {
		return this.disconnectButton;
	}
}
