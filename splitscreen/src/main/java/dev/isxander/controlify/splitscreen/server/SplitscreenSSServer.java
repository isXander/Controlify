package dev.isxander.controlify.splitscreen.server;

import dev.isxander.controlify.splitscreen.server.login.SplitscreenLoginFlowServer;
import dev.isxander.controlify.splitscreen.server.play.sound.ClientboundBundledSoundEntityPacket;
import dev.isxander.controlify.splitscreen.server.play.sound.ClientboundBundledSoundPacket;
import dev.isxander.controlify.splitscreen.server.play.sound.ClientboundSetBundleStatePacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSoundEntityPacket;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SplitscreenSSServer {
    private static final Map<Class<?>, PacketBundler<?, ?>> BUNDLERS = new HashMap<>();

    public static void init() {
        registerPackets();

        SplitscreenLoginFlowServer.init();
    }

    private static void registerPackets() {
        var clientbound = PayloadTypeRegistry.playS2C();
        var serverbound = PayloadTypeRegistry.playC2S();

        clientbound.register(ClientboundSetBundleStatePacket.TYPE, ClientboundSetBundleStatePacket.STREAM_CODEC);

        clientbound.register(ClientboundBundledSoundPacket.TYPE, ClientboundBundledSoundPacket.STREAM_CODEC);
        registerBundler(new PacketBundler.Simple<>(ClientboundSoundPacket.class, ClientboundBundledSoundPacket::new));

        clientbound.register(ClientboundBundledSoundEntityPacket.TYPE, ClientboundBundledSoundEntityPacket.STREAM_CODEC);
        registerBundler(new PacketBundler.Simple<>(ClientboundSoundEntityPacket.class, ClientboundBundledSoundEntityPacket::new));
    }

    public static <T extends Packet<?>> Optional<PacketBundler<T, ?>> getBundler(Class<T> clazz) {
        return Optional.ofNullable((PacketBundler<T, ?>) BUNDLERS.get(clazz));
    }


    private static void registerBundler(PacketBundler<?, ?> bundler) {
        BUNDLERS.put(bundler.packetClass(), bundler);
    }
}
