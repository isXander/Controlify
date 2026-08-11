/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import dev.isxander.controlify.gametest.framework.SystemToastDuck;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(SystemToast.class)
public class SystemToastMixin implements SystemToastDuck {

	@Unique private Component controlify_test$cachedMessage;

	//? if >=26.2 {
	@Unique private Component controlify_test$cachedTitle;

	@Inject(
		method = "update(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;)V",
		at = @At("HEAD")
	)
	private void cacheText(Component title, Component message, CallbackInfo ci) {
		this.controlify_test$cachedTitle = title;
		this.controlify_test$cachedMessage = message;
	}

	@Override
	public Component controlify_test$getTitle() {
		return controlify_test$cachedTitle;
	}
	//?} else {
	/*@Shadow private Component title;

	@Inject(
		method = "<init>(Lnet/minecraft/client/gui/components/toasts/SystemToast$SystemToastId;Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;)V",
		at = @At("RETURN")
	)
	private void cacheTextCtr(SystemToast.SystemToastId id, Component title, Component message, CallbackInfo ci) {
		this.controlify_test$cachedMessage = message;
	}

	@WrapOperation(
		method = "multiline",
		at = @At(
			value = "NEW",
			target = "(Lnet/minecraft/client/gui/components/toasts/SystemToast$SystemToastId;Lnet/minecraft/network/chat/Component;Ljava/util/List;I)Lnet/minecraft/client/gui/components/toasts/SystemToast;"
		)
	)
	private static SystemToast cacheTextStatic(
		SystemToast.SystemToastId id, Component title, List<FormattedCharSequence> messageLines, int width,
		Operation<SystemToast> original,
		@Local(argsOnly = true, name = "message") Component message
	) {
		var toast = original.call(id, title, messageLines, width);
		((SystemToastMixin) (Object) toast).controlify_test$cachedMessage = message;
		return toast;
	}

	@Inject(method = "reset", at = @At("RETURN"))
	private void cacheTextReset(Component title, Component message, CallbackInfo ci) {
		this.controlify_test$cachedMessage = message;
	}

	@Override
	public Component controlify_test$getTitle() {
		return title;
	}
	*///?}

	@Override
	public Component controlify_test$getMessage() {
		return controlify_test$cachedMessage;
	}
}
