package dev.isxander.splitscreen.config;

import com.mojang.serialization.Codec;
import dev.isxander.yacl3.config.v3.ConfigEntry;
import dev.isxander.yacl3.config.v3.JsonFileCodecConfig;
import net.fabricmc.loader.api.FabricLoader;

@SuppressWarnings("UnstableApiUsage")
public class SplitscreenServerConfig extends JsonFileCodecConfig<SplitscreenServerConfig> {
    public static final SplitscreenServerConfig INSTANCE = new SplitscreenServerConfig();

    public SplitscreenServerConfig() {
        super(FabricLoader.getInstance().getConfigDir().resolve("splitscreen_server.json"));
    }

    /**
     * Amount of sub-players that a server allows to connect from a single account.
     * This is the maximum amount of sub-players that can be connected to a single account.
     */
    public final ConfigEntry<Integer> maxClients =
            register("max_clients", 3, Codec.INT);

    /**
     * If the sub-players can pick any username they like, e.g. 'Notch', when false, it will be OriginalUsername1/2/3
     * This is highly discouraged to enable on public servers, as sub-players could impersonate other players.
     */
    public final ConfigEntry<Boolean> allowAnyUsername =
            register("allow_any_username", false, Codec.BOOL);

    /**
     * Allow splitscreen logins after the main player has logged in and is already playing.
     * This increases risk because at login the client guarantees the amount of sub-players it will use,
     * but if the main player is already in the game, the host client does not ensure that the amount of sub-players
     * is what it expects.
     */
    public final ConfigEntry<Boolean> allowLateLogins =
            register("allow_late_logins", false, Codec.BOOL);
}
