package dev.isxander.controlify.api.guide;

import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.phys.HitResult;

public record InGameCtx(
        Minecraft client,
        LocalPlayer player,
        ClientLevel level,
        HitResult hitResult,
        ControllerEntity controller,
        GuideVerbosity verbosity
) implements FactCtx {
}
