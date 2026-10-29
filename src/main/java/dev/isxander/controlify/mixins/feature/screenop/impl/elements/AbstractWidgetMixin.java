/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.elements;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ScreenRuntime;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.input.MouseButtonEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(AbstractWidget.class)
public class AbstractWidgetMixin {
	@WrapWithCondition(
			method = "mouseClicked",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/AbstractWidget;onClick(Lnet/minecraft/client/input/MouseButtonEvent;Z)V"
			)
	)
	private boolean openKeyboardInVmouseMode(AbstractWidget instance, MouseButtonEvent event, boolean doubleClick) {
		assert MinecraftUtil.getScreen() != null;

		Controlify controlify = Controlify.instance();
		ControllerImpl controller = controlify.getCurrentController().orElse(null);

		if (controller != null && ScreenRuntime.virtualMouse().map(mouse -> mouse.isEnabled()).orElse(false)) {
			var context = ScreenRuntime.current();
			return context == null || !context.tryOpenKeyboard(controller, (AbstractWidget) (Object) this);
		}

		return true;
	}
}
