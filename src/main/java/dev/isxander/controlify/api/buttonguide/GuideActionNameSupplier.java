package dev.isxander.controlify.api.buttonguide;

import dev.isxander.controlify.controller.Controller;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.HitResult;

import java.util.Optional;

/**
 * Supplies the text to display for a guide action based on the current context.
 * If return is empty, the action will not be displayed.
 */
@FunctionalInterface
public interface GuideActionNameSupplier {
    Optional<Component> supply(
            Minecraft client,
            LocalPlayer player,
            ClientLevel level,
            HitResult hitResult,
            Controller<?, ?> controller
    );
}
