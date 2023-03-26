package dev.isxander.controlify.api.ingameguide;

import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * Supplies the text to display for a guide action based on the current context.
 * If return is empty, the action will not be displayed.
 * <p>
 * This is supplied once every tick.
 */
@FunctionalInterface
public interface GuideActionNameSupplier {
    Optional<Component> supply(IngameGuideContext ctx);
}
