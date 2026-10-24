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
import dev.isxander.controlify.api.gui.ButtonGuidePredicate;
import dev.isxander.controlify.impl.action.ControlifyActions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(SelectWorldScreen.class)
public class SelectWorldScreenMixin {

	@Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
	@Definition(id = "GUI_BACK", field = "Lnet/minecraft/network/chat/CommonComponents;GUI_BACK:Lnet/minecraft/network/chat/Component;")
	@Definition(id = "build", method = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;")
	@Expression("builder(GUI_BACK, ?).?(?).build()")
	@ModifyExpressionValue(method = "createFooterButtons", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Button modifyCancelButton(Button button) {
		dev.isxander.controlify.impl.screenop.ScreenContextImpl.get((SelectWorldScreen) (Object) this).buttonGuide().addGuideToButton(
				button,
				ControlifyActions.GUI_BACK,
				ButtonGuidePredicate.always()
		);
		return button;
	}

	@Definition(id = "builder", method = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;")
	@Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
	@Definition(id = "build", method = "Lnet/minecraft/client/gui/components/Button$Builder;build()Lnet/minecraft/client/gui/components/Button;")
	@Expression("builder(translatable('selectWorld.create'), ?).build()")
	@ModifyExpressionValue(method = "createFooterButtons", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Button modifyCreateButton(Button button) {
		dev.isxander.controlify.impl.screenop.ScreenContextImpl.get((SelectWorldScreen) (Object) this).buttonGuide().addGuideToButton(
				button,
				ControlifyActions.GUI_ABSTRACT_ACTION_1,
				ButtonGuidePredicate.always()
		);
		return button;
	}
}
