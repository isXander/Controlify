package dev.isxander.controlify.mixins.feature.guide.ingame;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Player.class)
public interface PlayerAccessor {
    //? if >=1.21.2 {
    @Invoker
    boolean callCanGlide();
    //?}
}
