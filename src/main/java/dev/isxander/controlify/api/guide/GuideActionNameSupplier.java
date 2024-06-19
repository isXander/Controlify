package dev.isxander.controlify.api.guide;

import net.minecraft.network.chat.Component;

import java.util.Optional;

/**
 * Supplies the text to display for a guide action based on the current id.
 * If return is empty, the action will not be displayed.
 * <p>
 * This is supplied once every tick.
 */
@FunctionalInterface
public interface GuideActionNameSupplier<T> {
    Optional<Component> supply(T ctx);
}
