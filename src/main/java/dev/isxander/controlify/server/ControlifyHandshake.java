package dev.isxander.controlify.server;

import com.mojang.logging.LogUtils;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.slf4j.Logger;

import java.util.concurrent.CompletableFuture;

public class ControlifyHandshake {
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final int PROTOCOL_VERSION = 1;
    public static final ResourceLocation HANDSHAKE_CHANNEL = new ResourceLocation("controlify", "handshake");

    public static void setupOnServer() {
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            FriendlyByteBuf buf = PacketByteBufs.create();
            buf.writeInt(PROTOCOL_VERSION);
            sender.sendPacket(HANDSHAKE_CHANNEL, buf);
        });

        ServerLoginNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL, (server, handler, understood, buf, synchronizer, responseSender) -> {
            if (!understood) {
                // client does not have controlify installed
                return;
            }

            int clientProtocolVersion = buf.readInt();
            if (clientProtocolVersion > PROTOCOL_VERSION) {
                handler.disconnect(Component.literal("Server has an old version of Controlify installed and is incompatible with this client.").withStyle(ChatFormatting.RED));
            } else if (clientProtocolVersion < PROTOCOL_VERSION) {
                handler.disconnect(Component.literal("Client has an old version of Controlify installed and is incompatible with this server.").withStyle(ChatFormatting.RED));
            }
        });
    }

    public static void setupOnClient() {
        ClientLoginNetworking.registerGlobalReceiver(HANDSHAKE_CHANNEL, (client, handler, buf, listenerAdder) -> {
            LOGGER.info("Server has Controlify. Responding with handshake.");
            FriendlyByteBuf response = PacketByteBufs.create();
            response.writeInt(PROTOCOL_VERSION);

            return CompletableFuture.completedFuture(response);
        });
    }
}
