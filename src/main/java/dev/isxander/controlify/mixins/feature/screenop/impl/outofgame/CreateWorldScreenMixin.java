/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.outofgame;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import dev.isxander.controlify.impl.screenop.behaviours.vanilla.CreateWorldScreenBehaviour;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Slice;

@Mixin(CreateWorldScreen.class)
public abstract class CreateWorldScreenMixin implements ScreenBehaviourFactory<CreateWorldScreen> {
	@Shadow
	protected abstract void onCreate();

	@ModifyExpressionValue(
			method = "init()V",
			slice = @Slice(
					from = @At(
							value = "CONSTANT",
							args = "stringValue=selectWorld.create"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;",
					ordinal = 0
			)
	)
	private Button modifyCreateButton(Button button) {
		ScreenContextImpl.get((net.minecraft.client.gui.screens.Screen) (Object) this).buttonGuide().addGuideToButton(
				button,
				ControlifyActions.GUI_ABSTRACT_ACTION_1,
				ButtonGuidePredicate.always()
		);
		return button;
	}

	@ModifyExpressionValue(
			method = "init()V",
			slice = @Slice(
					from = @At(
							value = "FIELD",
							target = "Lnet/minecraft/network/chat/CommonComponents;GUI_CANCEL:Lnet/minecraft/network/chat/Component;"
					)
			),
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;",
					ordinal = 0
			)
	)
	private Button modifyCancelButton(Button button) {
		ScreenContextImpl.get((net.minecraft.client.gui.screens.Screen) (Object) this).buttonGuide().addGuideToButton(
				button,
				ControlifyActions.GUI_BACK,
				ButtonGuidePredicate.always()
		);
		return button;
	}

	@Override
	public ScreenBehaviour create(@NonNull ScreenContext<? extends CreateWorldScreen> context) {
		return new CreateWorldScreenBehaviour(context, this::onCreate);
	}
}
