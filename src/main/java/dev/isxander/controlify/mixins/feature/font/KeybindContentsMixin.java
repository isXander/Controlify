/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.font;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.gui.font.ActionGlyphsImpl;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.KeybindContents;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;

@Mixin(KeybindContents.class)
public class KeybindContentsMixin {
	@Shadow
	@Final
	private String name;

	@Unique
	private @Nullable Identifier nameAsIdentifier;

	@Inject(method = "<init>", at = @At("RETURN"))
	private void parseNameAsIdentifier(String name, CallbackInfo ci) {
		this.nameAsIdentifier = Identifier.tryParse(name);
	}

	@WrapMethod(method = "visit(Lnet/minecraft/network/chat/FormattedText$StyledContentConsumer;Lnet/minecraft/network/chat/Style;)Ljava/util/Optional;")
	private <T> Optional<T> replaceWrapperFont(FormattedText.StyledContentConsumer<T> output, Style currentStyle, Operation<Optional<T>> original) {
		boolean wrapperFont = currentStyle.getFont() instanceof FontDescription.Resource(Identifier font)
							  && ActionGlyphsImpl.WRAPPER_FONT.equals(font);

		if (wrapperFont) {
			Style styleWithoutWrapper = currentStyle.withFont(null);

			if (this.nameAsIdentifier != null) {
				Optional<Component> inputText = Controlify.instance().getCurrentController()
						.map(c -> Controlify.instance().inputFontMapper()
								.getComponentFromAction(
										c.info().type().id(),
										c.input().getAction(this.nameAsIdentifier)
								)
						);
				if (inputText.isPresent()) {
					return inputText.get().visit(output, styleWithoutWrapper);
				}
			}

			// NEVER return original when wrapper font is detected.
			// wrapper font is not a defined font and will display as squares
			return Component.literal(this.name).visit(output, styleWithoutWrapper.withItalic(true).withBold(true));
		}

		return original.call(output, currentStyle);
	}
}
