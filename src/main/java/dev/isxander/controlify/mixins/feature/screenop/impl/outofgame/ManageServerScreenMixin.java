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
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.ServerAddressBehaviour;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.ManageServerScreen;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ManageServerScreen.class)
public class ManageServerScreenMixin implements ScreenBehaviourFactory<ManageServerScreen>, dev.isxander.controlify.impl.screenop.behaviours.vanilla.ServerAddressScreenAccess {

	@Shadow
	private EditBox ipEdit;
	@Shadow
	private Button addButton;
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
	public ScreenBehaviour create(@NonNull ScreenContext<? extends ManageServerScreen> context) {
		return new ServerAddressBehaviour(context,
				() -> this.addButton,
				() -> this.cancelButton
		);
	}

	@Override
	public EditBox controlify$getAddressField() {
		return this.ipEdit;
	}

}
