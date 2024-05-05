package dev.isxander.controlify.mixins.feature.splitscreen;

import com.mojang.blaze3d.platform.ScreenManager;
import com.mojang.blaze3d.platform.Window;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Window.class)
public interface WindowAccessor {
    @Accessor
    ScreenManager getScreenManager();
}
