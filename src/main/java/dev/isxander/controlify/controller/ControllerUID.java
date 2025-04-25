package dev.isxander.controlify.controller;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the unique identifier Controlify generates to represent a controller.
 * This UID is used to identify controllers across sessions and is not the same as the
 * {@link dev.isxander.controlify.controllermanager.UniqueControllerID} provided by the underlying driver.
 * @param string The string representation of the UID.
 */
public record ControllerUID(@NotNull String string) {
    public static @Nullable ControllerUID fromNullable(@Nullable String value) {
        return value == null ? null : new ControllerUID(value);
    }
}
