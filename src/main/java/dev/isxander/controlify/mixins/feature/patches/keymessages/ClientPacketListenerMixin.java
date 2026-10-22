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
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.action.ControlifyActions;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

@Mixin(ClientPacketListener.class)
public class ClientPacketListenerMixin {
	/**
	 * Change 'Press [key]' to 'Press [controller button]' when adding dismount tip.
	 */
	@Definition(id = "keyShift", field = "Lnet/minecraft/client/Options;keyShift:Lnet/minecraft/client/KeyMapping;")
	@Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
	@Expression("?.keyShift.getTranslatedKeyMessage()")
	@ModifyExpressionValue(method = "handleSetEntityPassengersPacket", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Component useControllerTextForSneakTip(Component original) {
		return Controlify.instance().getCurrentController()
				.flatMap(c -> Controlify.instance().currentInputMode().isController() ? Optional.of(c) : Optional.empty())
				.flatMap(c -> Optional.ofNullable(ControlifyActions.SNEAK.onOrNull(c)))
				.map(a -> ActionGlyphs.action(a.spec().id()))
				.orElse(original);
	}

	@Definition(id = "keyUp", field = "Lnet/minecraft/client/Options;keyUp:Lnet/minecraft/client/KeyMapping;")
	@Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
	@Expression("?.keyUp.getTranslatedKeyMessage()")
	@ModifyExpressionValue(method = "handleGameEvent", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Component useTutorialUpGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.WALK_FORWARD.actionId(), original);
	}

	@Definition(id = "keyLeft", field = "Lnet/minecraft/client/Options;keyLeft:Lnet/minecraft/client/KeyMapping;")
	@Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
	@Expression("?.keyLeft.getTranslatedKeyMessage()")
	@ModifyExpressionValue(method = "handleGameEvent", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Component useTutorialLeftGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.WALK_LEFT.actionId(), original);
	}

	@Definition(id = "keyDown", field = "Lnet/minecraft/client/Options;keyDown:Lnet/minecraft/client/KeyMapping;")
	@Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
	@Expression("?.keyDown.getTranslatedKeyMessage()")
	@ModifyExpressionValue(method = "handleGameEvent", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Component useTutorialDownGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.WALK_BACKWARD.actionId(), original);
	}

	@Definition(id = "keyRight", field = "Lnet/minecraft/client/Options;keyRight:Lnet/minecraft/client/KeyMapping;")
	@Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
	@Expression("?.keyRight.getTranslatedKeyMessage()")
	@ModifyExpressionValue(method = "handleGameEvent", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Component useTutorialRightGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.WALK_RIGHT.actionId(), original);
	}

	@Definition(id = "keyJump", field = "Lnet/minecraft/client/Options;keyJump:Lnet/minecraft/client/KeyMapping;")
	@Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
	@Expression("?.keyJump.getTranslatedKeyMessage()")
	@ModifyExpressionValue(method = "handleGameEvent", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Component useTutorialJumpGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.JUMP.actionId(), original);
	}

	@Definition(id = "keyInventory", field = "Lnet/minecraft/client/Options;keyInventory:Lnet/minecraft/client/KeyMapping;")
	@Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
	@Expression("?.keyInventory.getTranslatedKeyMessage()")
	@ModifyExpressionValue(method = "handleGameEvent", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Component useTutorialInventoryGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.INVENTORY.actionId(), original);
	}

	@Definition(id = "keyScreenshot", field = "Lnet/minecraft/client/Options;keyScreenshot:Lnet/minecraft/client/KeyMapping;")
	@Definition(id = "getTranslatedKeyMessage", method = "Lnet/minecraft/client/KeyMapping;getTranslatedKeyMessage()Lnet/minecraft/network/chat/Component;")
	@Expression("?.keyScreenshot.getTranslatedKeyMessage()")
	@ModifyExpressionValue(method = "handleGameEvent", at = @At("MIXINEXTRAS:EXPRESSION"))
	private Component useTutorialScreenshotGlyph(Component original) {
		return ActionGlyphs.actionWithActiveFallback(ControlifyActions.TAKE_SCREENSHOT.actionId(), original);
	}
}
