package dev.isxander.splitscreen.server.status;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.splitscreen.util.CSUtil;
import dev.isxander.splitscreen.util.CodecExtensionHelper;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.network.protocol.status.ServerStatus;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.IntStream;

/**
 * Additional data attached to {@link ServerStatus} that the server sends when clients ping them.
 * <p>
 * An additional object is serialized in the {@link ServerStatus} JSON with the key <code>isxander_splitscreen</code>.
 * @param supportedProtocols a list of all the splitscreen protocols this server supports
 * @param maxSubPlayers the amount of sub-players a single client is permitted to join with
 */
public record ServerStatusSplitscreenExt(int[] supportedProtocols, int maxSubPlayers) {
    public static final ResourceLocation SPLITSCREEN_SUPPORTED_SPRITE = CSUtil.rl("splitscreen_supported");
    public static final ResourceLocation SPLITSCREEN_UNSUPPORTED_SPRITE = CSUtil.rl("splitscreen_unsupported");

    public static final Codec<ServerStatusSplitscreenExt> CODEC = RecordCodecBuilder.create(
            instance -> instance.group(
                            Codec.INT_STREAM.fieldOf("supported_protocols")
                                    .xmap(IntStream::toArray, IntStream::of)
                                    .forGetter(ServerStatusSplitscreenExt::supportedProtocols),
                            Codec.INT.optionalFieldOf("max_sub_players", -1)
                                    .forGetter(ServerStatusSplitscreenExt::maxSubPlayers)
                    )
                    .apply(instance, ServerStatusSplitscreenExt::new)
    );
    public static final Codec<Optional<ServerStatusSplitscreenExt>> CODEC_FIELD_OPT = CODEC.lenientOptionalFieldOf("isxander_splitscreen").codec();

    public static final ThreadLocal<ServerStatusSplitscreenExt> inProgressParam = new ThreadLocal<>();

    public static Codec<ServerStatus> wrapCodec(Codec<ServerStatus> codec) {
        return CodecExtensionHelper.buildExtensionCodec(codec, CODEC_FIELD_OPT, ServerStatusSplitscreenExt::getExt, inProgressParam);
    }

    public static ServerStatus copyWithExt(ServerStatus vanilla, ServerStatusSplitscreenExt ext) {
        return construct(() -> new ServerStatus(vanilla.description(), vanilla.players(), vanilla.version(), vanilla.favicon(), vanilla.enforcesSecureChat()), ext);
    }

    public static <T> T construct(Supplier<T> ctor, ServerStatusSplitscreenExt ext) {
        try {
            inProgressParam.set(ext);
            return ctor.get();
        } finally {
            inProgressParam.remove();
        }
    }

    public static Optional<ServerStatusSplitscreenExt> getExt(@NotNull ServerStatus vanilla) {
        return getExt0(vanilla);
    }

    public static Optional<ServerStatusSplitscreenExt> getExt(@NotNull ServerData vanilla) {
        return getExt0(vanilla);
    }

    public static void setExt(@NotNull ServerData vanilla, @Nullable ServerStatusSplitscreenExt ext) {
        setExt0(vanilla, ext);
    }

    private static Optional<ServerStatusSplitscreenExt> getExt0(@NotNull Object vanilla) {
        var duck = (ServerStatusSplitscreenExt.Duck) vanilla;
        return Optional.ofNullable(duck.splitscreen$getExt());
    }

    private static void setExt0(@NotNull Object vanilla, @Nullable ServerStatusSplitscreenExt ext) {
        var duck = (ServerStatusSplitscreenExt.Duck) vanilla;
        duck.splitscreen$setExt(ext);
    }

    public interface Duck {
        ServerStatusSplitscreenExt splitscreen$getExt();

        void splitscreen$setExt(ServerStatusSplitscreenExt ext);
    }
}
