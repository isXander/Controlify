package dev.isxander.controlify.splitscreen.config;

import com.mojang.serialization.Codec;
import dev.isxander.controlify.splitscreen.configsync.ConfigSyncRegistry;
import dev.isxander.controlify.splitscreen.util.CSUtil;
import dev.isxander.yacl3.config.v3.ConfigEntry;
import dev.isxander.yacl3.config.v3.JsonFileCodecConfig;
import dev.isxander.yacl3.config.v3.ReadonlyConfigEntry;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.resources.ResourceLocation;

@SuppressWarnings("UnstableApiUsage")
public class SplitscreenConfig extends JsonFileCodecConfig<SplitscreenConfig> {
    public static final SplitscreenConfig INSTANCE = new SplitscreenConfig();
    public static final ResourceLocation CONFIG_ID = CSUtil.rl("config");

    public SplitscreenConfig() {
        super(FabricLoader.getInstance().getConfigDir().resolve("splitscreen.json"));
    }

    /**
     * How splitscreen should be arranged, e.g. left/right vs top/bottom
     * <p>
     * Default: <code>false</code>
     */
    public final ConfigEntry<Boolean> preferVerticalSplitscreen =
            register("prefer_vertical_splitscreen", false, Codec.BOOL);

    /**
     * How sound events are managed between splitscreen instances.
     * <p>
     * Default: {@link AudioMethod#CLOSEST_ORIGIN}
     */
    public final ConfigEntry<AudioMethod> audioMethod =
            register("audio_method", AudioMethod.CLOSEST_ORIGIN, AudioMethod.CODEC);

    /**
     * How music is played across all players in splitscreen.
     * <p>
     * Default: {@link MusicMethod#FIRST_PLAYER}
     */
    public final ConfigEntry<MusicMethod> musicMethod =
            register("music_method", MusicMethod.FIRST_PLAYER, MusicMethod.CODEC);

    /**
     * Creates a record ready to send over the network to the server.
     */
    public SplitscreenServerSharedConfig createSharedConfig() {
        return new SplitscreenServerSharedConfig(audioMethod.get());
    }

    /**
     * Server-side (LAN) config.
     */
    public final ReadonlyConfigEntry<SplitscreenServerConfig> serverConfig =
            register("server", SplitscreenServerConfig.INSTANCE);

    @Override
    public void saveToFile() {
        super.saveToFile();
        ConfigSyncRegistry.onSave(CONFIG_ID);
    }
}
