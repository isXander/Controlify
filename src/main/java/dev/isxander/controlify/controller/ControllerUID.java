package dev.isxander.controlify.controller;

import com.mojang.serialization.Codec;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the unique identifier Controlify generates to represent a controller.
 * This UID is used to identify controllers across sessions and is not the same as the
 * {@link dev.isxander.controlify.controllermanager.UniqueControllerID} provided by the underlying driver.
 * @param string The string representation of the UID.
 */
public record ControllerUID(@NotNull String string) {
    public static final Codec<ControllerUID> CODEC = Codec.STRING.xmap(ControllerUID::new, ControllerUID::string);

    public static @Nullable ControllerUID fromNullableString(@Nullable String value) {
        return value == null ? null : new ControllerUID(value);
    }

    public static @Nullable String toNullableString(@Nullable ControllerUID uid) {
        return uid == null ? null : uid.string();
    }
}
