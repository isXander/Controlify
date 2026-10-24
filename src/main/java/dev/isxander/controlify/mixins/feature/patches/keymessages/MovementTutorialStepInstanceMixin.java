/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.patches.keymessages;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.controlify.api.font.ActionGlyphs;
import dev.isxander.controlify.impl.action.ControlifyActions;
import net.minecraft.client.tutorial.MovementTutorialStepInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(MovementTutorialStepInstance.class)
public class MovementTutorialStepInstanceMixin {
	@Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
	@Expression("key('forward')")
	@ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static Component useForwardGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.WALK_FORWARD.actionId(), original);
	}

	@Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
	@Expression("key('left')")
	@ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static Component useLeftGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.WALK_LEFT.actionId(), original);
	}

	@Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
	@Expression("key('back')")
	@ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static Component useBackGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.WALK_BACKWARD.actionId(), original);
	}

	@Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
	@Expression("key('right')")
	@ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static Component useRightGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.WALK_RIGHT.actionId(), original);
	}

	@Definition(id = "key", method = "Lnet/minecraft/client/tutorial/Tutorial;key(Ljava/lang/String;)Lnet/minecraft/network/chat/Component;")
	@Expression("key('jump')")
	@ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static Component useJumpGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.JUMP.actionId(), original);
	}

	@Definition(id = "translatable", method = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;")
	@Expression("translatable('tutorial.look.description')")
	@ModifyExpressionValue(method = "<clinit>", at = @At("MIXINEXTRAS:EXPRESSION"))
	private static MutableComponent useLookMessage(MutableComponent original) {
		return Component.translatable("controlify.tutorial.look.description");
	}
}
