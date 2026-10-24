/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.font;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.font.ActionGlyphsImpl;
import net.minecraft.network.chat.contents.TranslatableContents;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(TranslatableContents.class)
public class TranslatableContentsMixin {
	@ModifyExpressionValue(
			method = "decompose",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/network/chat/contents/TranslatableContents;key:Ljava/lang/String;",
					opcode = Opcodes.GETFIELD
			)
	)
	private String replaceControllerActiveKey(String originalKey) {
		if (ActionGlyphsImpl.PLACEHOLDER_CONTROLLER_ACTIVE_KEY.equals(originalKey)) {
			if (Controlify.instance().currentInputMode().isController()) {
				return ActionGlyphsImpl.PLACEHOLDER_KEY;
			} else {
				// because the key doesn't exist, it will rely on the fallback text provided by this translatable contents
				return originalKey;
			}
		}

		return originalKey;
	}
}
