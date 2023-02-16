package dev.isxander.controlify.ingame.guide;

import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

@FunctionalInterface
public interface ButtonActionSupplier {
    Optional<GuideAction> supply(Minecraft client, LocalPlayer player, ClientLevel level, HitResult hitResult, Controller<?, ?> controller);
}
