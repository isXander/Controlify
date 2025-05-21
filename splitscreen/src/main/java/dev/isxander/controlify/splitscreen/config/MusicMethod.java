package dev.isxander.controlify.splitscreen.config;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * How music should be played across all players in splitscreen.
 * Some music tracks are dependent on location, since there are multiple players,
 * this can cause issues. This exists to define how to handle that.
 */
public enum MusicMethod implements StringRepresentable {
    /**
     * Only the first player is taken into account when playing music tracks.
     * @implNote other players music track will be muted.
     */
    FIRST_PLAYER("first_player"),

    /**
     * The most "urgent" music track will be played.
     * boss fight > biome > cave > regular
     */
    PRIORITY("priority");

    public static final Codec<MusicMethod> CODEC = StringRepresentable.fromEnum(MusicMethod::values);

    private final String name;

    MusicMethod(String name) {
        this.name = name;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.name;
    }
}
