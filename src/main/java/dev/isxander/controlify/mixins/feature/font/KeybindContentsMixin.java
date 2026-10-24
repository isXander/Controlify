/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.font;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.font.ActionGlyphsImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(KeybindContents.class)
public class KeybindContentsMixin {
	@Shadow
	@Final
	private String name;

	@WrapOperation(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/chat/contents/KeybindContents;getNestedComponent()Lnet/minecraft/network/chat/Component;"))
	private Component testVisitWithStyle(KeybindContents instance, Operation<Component> original, @Local(argsOnly = true, name = "currentStyle") Style style) {
		boolean wrapperFont = style.getFont() instanceof FontDescription.Resource(Identifier font)
				&& ActionGlyphsImpl.WRAPPER_FONT.equals(font);

		if (wrapperFont) {
			Optional<Component> inputText = Controlify.instance().getCurrentController()
					.map(c -> Controlify.instance().inputFontMapper()
							.getComponentFromAction(
									c.info().type().id(),
									c.input().getAction(Identifier.tryParse(this.name))
							)
					);
			if (inputText.isPresent()) {
				return inputText.get();
			}
		}

		return original.call(instance);
	}
}
