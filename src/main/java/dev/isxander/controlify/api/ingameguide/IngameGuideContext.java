package dev.isxander.controlify.api.ingameguide;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;

/**
 * All information available to a guide entry.
 * This may be added over time.
 * @param client the minecraft client
 * @param player local player
 * @param level the current world
 * @param hitResult where the player is currently looking
 * @param controller the controller for this guide renderer
 */
public record IngameGuideContext(Minecraft client,
                                 LocalPlayer player,
                                 ClientLevel level,
                                 HitResult hitResult,
                                 ControllerEntity controller) {
}
