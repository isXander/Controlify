/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.impl.elements;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import dev.isxander.controlify.api.screenop.ElementKeyboardBehaviour;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.gui.keyboard.CommonKeyboardHints;
import dev.isxander.controlify.impl.gui.keyboard.KeyboardOverlayScreen;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EditBox.class)
public abstract class EditBoxMixin extends AbstractWidget {

	@Shadow
	@Final
	private Font font;

	@Shadow
	private int textY;

	public EditBoxMixin(int x, int y, int width, int height, Component message) {
		super(x, y, width, height, message);
	}

	@ModifyExpressionValue(
			method = "extractWidgetRenderState",
			at = @At(
					value = "INVOKE",
					target = "Lnet/minecraft/client/gui/components/TextCursorUtils;isCursorVisible(J)Z",
					ordinal = 0
			)
	)
	private boolean decideKeyboardHint(
			boolean original,
			@Local(name = "displayed") String displayed,
			@Share("show_keyboard") LocalBooleanRef showKeyboardRef
	) {
		showKeyboardRef.set(false);
		ControllerImpl controller = Controlify.instance().getCurrentController().orElse(null);

		if (controller == null) {
			return original;
		}

		if (this.isFocused()
			&& displayed.isEmpty()
			&& controller.settings().generic.keyboard.showOnScreenKeyboard
			&& controller.settings().generic.guide.showScreenGuides
			&& Controlify.instance().currentInputMode().isController()
			&& !(MinecraftUtil.getScreen() instanceof KeyboardOverlayScreen)
			&& controlify$hasKeyboardBehaviour()
		) {
			showKeyboardRef.set(true);
			return false;
		}

		return original;
	}

	@Definition(id = "hint", field = "Lnet/minecraft/client/gui/components/EditBox;hint:Lnet/minecraft/network/chat/Component;")
	@Expression("this.hint != null")
	@Inject(method = "extractWidgetRenderState", at = @At(value = "MIXINEXTRAS:EXPRESSION", ordinal = 0))
	private void extractKeyboardHintText(
			GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a,
			CallbackInfo ci,
			@Local(name = "color") int color,
			@Local(name = "drawX") int drawX,
			@Share("show_keyboard") LocalBooleanRef showKeyboardRef
	) {
		if (showKeyboardRef.get()) {
			graphics.text(font, CommonKeyboardHints.OPEN_KEYBOARD.getComponent(), drawX, textY, color);
		}
	}

	@Unique private boolean controlify$hasKeyboardBehaviour() {
		var screen = MinecraftUtil.getScreen();
		if (screen == null) {
			return false;
		}
		var context = ScreenContextImpl.get(screen);
		return context.elementBehaviour((EditBox) (Object) this).keyboardBehaviour(context)
				.orElse(null) instanceof ElementKeyboardBehaviour.Handled;
	}

}
