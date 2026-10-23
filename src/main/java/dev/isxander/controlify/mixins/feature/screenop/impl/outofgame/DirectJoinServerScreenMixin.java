/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.ServerAddressScreenAccess;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.DirectJoinServerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(DirectJoinServerScreen.class)
public class DirectJoinServerScreenMixin implements ServerAddressScreenAccess {
	@Shadow
	private EditBox ipEdit;
	@Shadow
	private Button selectButton;
	@Unique
	private Button cancelButton;

	@Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
	@Definition(id = "GUI_CANCEL", field = "Lnet/minecraft/network/chat/CommonComponents;GUI_CANCEL:Lnet/minecraft/network/chat/Component;")
	@Definition(id = "build", method = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;")
	@Expression("builder(GUI_CANCEL, ?).?(?, ?, ?, ?).build()")
	@ModifyExpressionValue(method = "init", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Button captureCancelButton(Button button) {
		return this.cancelButton = button;
	}

	@Override
	public EditBox controlify$getAddressField() {
		return this.ipEdit;
	}

	@Override
	public Button controlify$getDoneButton() {
		return this.selectButton;
	}

	@Override
	public Button controlify$getBackButton() {
		return this.cancelButton;
	}
}
