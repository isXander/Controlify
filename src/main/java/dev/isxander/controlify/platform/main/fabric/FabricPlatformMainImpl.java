//? if fabric {
package dev.isxander.controlify.platform.main.fabric;

import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.main.PlatformMainUtilImpl;
import dev.isxander.controlify.platform.main.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.platform.main.events.HandshakeCompletionEvent;
import dev.isxander.controlify.platform.main.events.PlayerJoinedEvent;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FabricPlatformMainImpl implements PlatformMainUtilImpl {
    @Override
    public void registerCommandRegistrationCallback(CommandRegistrationCallbackEvent callback) {
        CommandRegistrationCallback.EVENT.register(callback::onRegister);
    }

    @Override
    public void registerInitPlayConnectionEvent(PlayerJoinedEvent event) {
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> event.onInit(handler.getPlayer()));
    }

    @Override
    public boolean isModLoaded(String... modIds) {
        return Arrays.stream(modIds).anyMatch(FabricLoader.getInstance()::isModLoaded);
    }

    @Override
    public void applyToControlifyEntrypoint(Consumer<ControlifyEntrypoint> entrypointConsumer) {
        FabricLoader.getInstance().getEntrypoints("controlify", ControlifyEntrypoint.class)
                .forEach(entrypointConsumer);
    }

    @Override
    public <I, O> void setupServersideHandshake(ResourceLocation handshakeId, ControlifyPacketCodec<I> serverBoundCodec, ControlifyPacketCodec<O> clientBoundCodec, Supplier<O> packetCreator, HandshakeCompletionEvent<I> completionEvent) {
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            O decodedPacket = packetCreator.get();

            FriendlyByteBuf encodedPacket = PacketByteBufs.create();
            clientBoundCodec.encode(encodedPacket, decodedPacket);

            sender.sendPacket(handshakeId, encodedPacket);
        });

        ServerLoginNetworking.registerGlobalReceiver(handshakeId, (server, handler, understood, buf, synchronizer, responseSender) -> {
            I decodedPacket = serverBoundCodec.decode(buf);

            completionEvent.onCompletion(decodedPacket, understood, handler);
        });
    }

    @Override
    public <T> Supplier<T> deferredRegister(Registry<T> registry, ResourceLocation id, Supplier<? extends T> registrant) {
        T registered = Registry.register(registry, id, registrant.get());
        return () -> registered;
    }

    @Override
    public Path getGameDir() {
        return FabricLoader.getInstance().getGameDir();
    }

    @Override
    public Path getConfigDir() {
        return FabricLoader.getInstance().getConfigDir();
    }

    @Override
    public boolean isDevEnv() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public Environment getEnv() {
        return switch (FabricLoader.getInstance().getEnvironmentType()) {
            case CLIENT -> Environment.CLIENT;
            case SERVER -> Environment.SERVER;
        };
    }

    @Override
    public String getControlifyVersion() {
        return FabricLoader.getInstance().getModContainer("controlify").orElseThrow()
                .getMetadata().getVersion().getFriendlyString();
    }
}
//?}
