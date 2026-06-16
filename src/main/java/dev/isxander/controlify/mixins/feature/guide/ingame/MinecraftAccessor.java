package dev.isxander.controlify.mixins.feature.guide.ingame;

import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Minecraft.class)
public interface MinecraftAccessor {
    @Invoker("pick")
    void controlify$invokePick(float partialTicks);
}
