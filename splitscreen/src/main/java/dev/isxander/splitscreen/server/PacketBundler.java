package dev.isxander.splitscreen.server;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.BiFunction;

/**
 * Registered in {@link SplitscreenSSServer}, it implements how to turn a packet meant for
 * multiple clients into a single packet sent to the controller within a splitscreen system.
 * @param <T> the unbundled packet (input)
 * @param <B> the bundled packet (output)
 */
public interface PacketBundler<T extends Packet<?>, B extends CustomPacketPayload> {
    Class<T> packetClass();

    B bundle(
            T packet,
            double x, double y, double z,
            double radius,
            ResourceKey<Level> dimension,
            SplitscreenPlayerInfo.Controller controller,
            List<ServerPlayer> players
    );

    record Simple<T extends Packet<?>, B extends CustomPacketPayload>(
            Class<? extends T> packetClassWild,
            BiFunction<BundledPacketInfo, T, B> bundleFunction
    ) implements PacketBundler<T, B> {
        @SuppressWarnings("unchecked")
        @Override
        public Class<T> packetClass() {
            return (Class<T>) packetClassWild;
        }

        @Override
        public B bundle(
                T packet,
                double x, double y, double z,
                double radius,
                ResourceKey<Level> dimension,
                SplitscreenPlayerInfo.Controller controller,
                List<ServerPlayer> players
        ) {
            var bundleInfo = BundledPacketInfo.create(controller, players);
            return this.bundleFunction.apply(bundleInfo, packet);
        }
    }
}
