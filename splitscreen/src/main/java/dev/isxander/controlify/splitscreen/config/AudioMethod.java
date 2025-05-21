package dev.isxander.controlify.splitscreen.config;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

/**
 * How positioned in-game sound events should be handled in splitscreen.
 */
public enum AudioMethod implements StringRepresentable {
    /**
     * Allows all clients to play their own audio, even when it will reduce
     * in duplicate sounds.
     */
    NOTHING("nothing"),

    /**
     * Sounds will only be played from the perspective of the first player.
     * If the players are far apart, the other players will appear muted.
     */
    FIRST_PLAYER_ONLY("first_player_only"),

    /**
     * Directional sounds will be played from the perspective of the closest player
     * to that sound source.
     */
    CLOSEST_ORIGIN("closest_origin"),

    /**
     * Sounds will be played from the perspective of the player that should be
     * most concerned about that sound.
     * For example, a block place sound will be played from the perspective of the player
     * who placed the block, a creeper hissing will be played from the perspective of the closest
     * player, or the averaged position of all players who should be concerned.
     */
    CONCERNING_PLAYER("concerning_player"),

    /**
     * Each player will play their own audio to a specified channel on the output.
     * For example, on a stereo output, the left player will play to the left channel.
     * This may sacrifice directionality of sounds since it is downsampled to mono.
     */
    UNIQUE_CHANNEL("unique_channel"),

    /**
     * Each player will play their own audio to a separate output device.
     */
    SEPARATE_OUTPUTS("separate_outputs"),;

    /**
     * Methods that require server-side support to function.
     */
    public static final AudioMethod[] SUPPORTING_SERVER_REQUIRED_METHODS = new AudioMethod[] {
            CLOSEST_ORIGIN,
            CONCERNING_PLAYER
    };

    public static final Codec<AudioMethod> CODEC = StringRepresentable.fromEnum(AudioMethod::values);

    private final String identifier;

    AudioMethod(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public @NotNull String getSerializedName() {
        return this.identifier;
    }
}
