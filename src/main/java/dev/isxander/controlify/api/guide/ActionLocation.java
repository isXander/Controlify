package dev.isxander.controlify.api.guide;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.NonNull;

/**
 * Whether the action should be on the left or right list.
 */
public enum ActionLocation implements StringRepresentable {
    LEFT("left"),
    RIGHT("right");

    private final String serialName;

    ActionLocation(String serialName) {
        this.serialName = serialName;
    }

    @Override
    public @NonNull String getSerializedName() {
        return this.serialName;
    }

    public static final Codec<ActionLocation> CODEC =
            StringRepresentable.fromEnum(ActionLocation::values);
}
