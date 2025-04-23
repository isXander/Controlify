package dev.isxander.controlify.splitscreen.mixins.core;

import com.mojang.blaze3d.platform.ScreenManager;
import net.minecraft.client.renderer.VirtualScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(VirtualScreen.class)
public interface VirtualScreenAccessor {
    @Accessor
    ScreenManager getScreenManager();
}
