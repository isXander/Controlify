package dev.isxander.splitscreen.mixins.engine.reparent;

import com.mojang.blaze3d.platform.Monitor;
import com.mojang.blaze3d.platform.ScreenManager;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(ScreenManager.class)
public interface ScreenManagerAccessor {
    @Accessor
    Long2ObjectMap<Monitor> getMonitors();
}
