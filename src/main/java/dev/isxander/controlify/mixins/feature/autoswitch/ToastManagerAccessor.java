package dev.isxander.controlify.mixins.feature.autoswitch;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

//? if >=1.21.2 {
import net.minecraft.client.gui.components.toasts.ToastManager;

@Mixin(ToastManager.class)
public interface ToastManagerAccessor {
    @Accessor
    List<ToastManager.ToastInstance<?>> getVisibleToasts();
}
//?} else {
/*import net.minecraft.client.gui.components.toasts.ToastComponent;

@Mixin(net.minecraft.client.gui.components.toasts.ToastComponent.class)
public interface ToastManagerAccessor {
    @Accessor("visible")
    List<ToastComponent.ToastInstance<?>> getVisibleToasts();
}
*///?}
