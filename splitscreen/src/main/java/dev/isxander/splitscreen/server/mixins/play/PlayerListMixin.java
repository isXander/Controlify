package dev.isxander.splitscreen.server.mixins.play;

import com.google.common.collect.Lists;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import dev.isxander.splitscreen.server.PacketBundler;
import dev.isxander.splitscreen.server.SplitscreenPlayerInfo;
import dev.isxander.splitscreen.server.SplitscreenSSServer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// reduced priority so WrapOperation will be the last so we can capture a modified packet args
@Mixin(value = PlayerList.class, priority = 2000)
public class PlayerListMixin {
    /**
     * Insert a list the top of broadcast so we can collect all the players we want to bundle
     */
    @Inject(method = "broadcast", at = @At("HEAD"))
    private void insertListAtHead(
            CallbackInfo ci,
            @Share("players_to_send") LocalRef<List<ServerPlayer>> playersToSendRef,
            @Share("packet_bundler") LocalRef<PacketBundler<?, ?>> packetBundlerRef,
            @Local(argsOnly = true) Packet<?> packet
    ) {
        SplitscreenSSServer.getBundler(packet.getClass()).ifPresent(bundler -> {
            packetBundlerRef.set(bundler);
            playersToSendRef.set(new ArrayList<>());
        });
    }

    /**
     * Prevents immediate sending of packets to clients and instead adds it
     * to the list that has been added to the locals in the above mixin handler.
     */
    @WrapOperation(
            method = "broadcast",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"
            )
    )
    private void collectSends(
            /* WrapOperation args */ ServerGamePacketListenerImpl instance, Packet<?> packet, Operation<Void> original,
            /* Iterating player   */ @Local ServerPlayer player,
            /* Sugar from above   */ @Share("players_to_send") LocalRef<List<ServerPlayer>> playersToSendRef
    ) {
        if (playersToSendRef.get() == null) {
            original.call(instance, packet);
            return;
        }

        SplitscreenPlayerInfo.get(player).ifPresentOrElse(
                info -> playersToSendRef.get().add(player),
                () -> original.call(instance, packet)
        );
    }

    /**
     * Injects after loop (at the end of the broadcast method) and bundles
     * packets and sends them to controllers, rather than sending them to all pawns and controllers.
     */
    @Inject(method = "broadcast", at = @At("RETURN"))
    private void bundlePackets(
            /* Method args */             Player except, double x, double y, double z, double radius, ResourceKey<Level> dimension, Packet<?> packet,
            /* @Inject CI */              CallbackInfo ci,
            /* All splitscreen players */ @Share("players_to_send") LocalRef<List<ServerPlayer>> playersToSendRef,
            /* The bundler to use */      @Share("packet_bundler") LocalRef<PacketBundler<?, ?>> packetBundlerRef
    ) {
        if (playersToSendRef.get() == null) {
            return;
        }

        // map of controllerinfo to all players involved with the broadcast, including the controller player
        // although Controller#subPlayers exists, they may have failed the distance check in the broadcast,
        // we encode who was meant to receive the sound to
        var map = new HashMap<SplitscreenPlayerInfo.Controller, List<ServerPlayer>>();

        // collate the flat list of players into map
        for (ServerPlayer player : playersToSendRef.get()) {
            switch (SplitscreenPlayerInfo.get(player).orElseThrow()) {
                case SplitscreenPlayerInfo.Controller controller ->
                        map.put(controller, Lists.newArrayList(player));
                case SplitscreenPlayerInfo.SubPlayer subPlayer ->
                        map.computeIfAbsent(subPlayer.controller(), c -> new ArrayList<>()).add(player);
            }
        }

        // bundle and send a packet for each splitscreen system
        map.forEach((controller, players) -> {
            CustomPacketPayload payload = doBundle(packet, packetBundlerRef.get(), x, y, z, radius, dimension, controller, players);
            ServerPlayNetworking.send(controller.player(), payload);
        });
    }

    // a hack to get around wildcard generics
    @SuppressWarnings("unchecked")
    @Unique
    private <T extends Packet<?>> CustomPacketPayload doBundle(
            Packet<?> packet, PacketBundler<T, ?> bundler,
            double x, double y, double z, double radius, ResourceKey<Level> dimension,
            SplitscreenPlayerInfo.Controller controller, List<ServerPlayer> players
    ) {
        return bundler.bundle((T) packet, x, y, z, radius, dimension, controller, players);
    }
}
