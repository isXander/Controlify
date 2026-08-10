package dev.isxander.controlify.gametest.mixin;

import dev.isxander.controlify.gametest.framework.SystemToastDuck;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(SystemToast.class)
public class SystemToastMixin implements SystemToastDuck {
	@Unique
	private Component controlify_test$cachedTitle;

	@Unique
	private Component controlify_test$cachedMessage;

	@Inject(method = "update(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/Component;)V", at = @At("HEAD"))
	private void cacheText(Component title, Component message, CallbackInfo ci) {
		this.controlify_test$cachedTitle = title;
		this.controlify_test$cachedMessage = message;
	}

	@Override
	public Component controlify_test$getTitle() {
		return controlify_test$cachedTitle;
	}

	@Override
	public Component controlify_test$getMessage() {
		return controlify_test$cachedMessage;
	}
}
