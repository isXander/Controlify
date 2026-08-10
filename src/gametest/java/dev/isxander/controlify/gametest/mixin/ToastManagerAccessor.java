package dev.isxander.controlify.gametest.mixin;

import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Deque;
import java.util.List;

@Mixin(ToastManager.class)
public interface ToastManagerAccessor {
	@Accessor("queued")
	Deque<Toast> controlify_test$getQueued();

	// ToastInstance is a private class
	@Accessor("visibleToasts")
	List<?> controlify_test$getVisibleToasts();
}
