package dev.isxander.controlify.api.ingameguide;

import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;

public record IngameGuideContext(Minecraft client,
                                 LocalPlayer player,
                                 ClientLevel level,
                                 HitResult hitResult,
                                 Controller<?, ?> controller) {
}
