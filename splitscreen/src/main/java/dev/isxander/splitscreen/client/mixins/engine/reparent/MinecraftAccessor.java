package dev.isxander.splitscreen.client.mixins.engine.reparent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.VirtualScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Invoker
    String callCreateTitle();

    @Accessor
    VirtualScreen getVirtualScreen();
}
