/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.PauseScreenProcessor;
import net.minecraft.client.gui.screens.PauseScreen;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

//? if >=26.4 {
import net.minecraft.client.gui.screens.reporting.DraftIconButton;
import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
//?} else {
import net.minecraft.client.gui.components.Button;
import org.spongepowered.asm.mixin.Shadow;
//?}

@Mixin(PauseScreen.class)
public class PauseScreenMixin implements ScreenProcessorProvider {

	//? if >=26.4 {
	@Unique private @Nullable DraftIconButton disconnectButton;

	@Definition(id = "addChild", method = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;I)Lnet/minecraft/client/gui/layouts/LayoutElement;")
	@Definition(id = "DraftIconButton", type = DraftIconButton.class)
	@Definition(id = "disconnectButtonLabel", method = "Lnet/minecraft/network/chat/CommonComponents;disconnectButtonLabel(Z)Lnet/minecraft/network/chat/Component;")
	@Expression("?.addChild(@(new DraftIconButton(?, ?, disconnectButtonLabel(?), ?, ?, ?)), ?)")
	@ModifyExpressionValue(method = "createPauseMenu", at = @At("MIXINEXTRAS:EXPRESSION"))
	private DraftIconButton captureDisconnectButton(DraftIconButton original) {
		return disconnectButton = original;
	}
	//?} else {
	/*@Shadow private @Nullable Button disconnectButton;
	*///?}

	@Unique private final PauseScreenProcessor processor =
			new PauseScreenProcessor((PauseScreen) (Object) this, () -> disconnectButton);

	@Override
	public ScreenProcessor<?> screenProcessor() {
		return processor;
	}
}
