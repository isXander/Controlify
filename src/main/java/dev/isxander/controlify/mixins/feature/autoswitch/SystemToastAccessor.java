package dev.isxander.controlify.mixins.feature.autoswitch;

import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.components.toasts.Toast;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(SystemToast.class)
public interface SystemToastAccessor {
    //? if >=1.21.2 {
    @Accessor
    void setWantedVisibility(Toast.Visibility visibility);
    //?}
}
