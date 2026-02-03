package dev.isxander.controlify.platform.main;

import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.main.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.platform.main.events.HandshakeCompletionEvent;
import dev.isxander.controlify.platform.main.events.PlayerJoinedEvent;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface PlatformMainUtilImpl {
    void registerCommandRegistrationCallback(CommandRegistrationCallbackEvent callback);

    void registerInitPlayConnectionEvent(PlayerJoinedEvent event);

    boolean isModLoaded(String... modIds);

    Path getGameDir();

    Path getConfigDir();

    boolean isDevEnv();

    Environment getEnv();

    String getControlifyVersion();

    void applyToControlifyEntrypoint(Consumer<ControlifyEntrypoint> entrypointConsumer);

    <I, O> void setupServersideHandshake(
            Identifier handshakeId,
            StreamCodec<FriendlyByteBuf, I> serverBoundCodec,
            StreamCodec<FriendlyByteBuf, O> clientBoundCodec,
            Supplier<O> packetCreator,
            HandshakeCompletionEvent<I> completionEvent
    );

    <T> Supplier<T> deferredRegister(Registry<T> registry, Identifier id, Supplier<? extends T> registrant);
}
