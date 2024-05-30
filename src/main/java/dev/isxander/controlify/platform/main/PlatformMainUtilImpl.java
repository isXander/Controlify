package dev.isxander.controlify.platform.main;

import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.main.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.platform.main.events.HandshakeCompletionEvent;
import dev.isxander.controlify.platform.main.events.InitPlayConnectionEvent;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public interface PlatformMainUtilImpl {
    void registerCommandRegistrationCallback(CommandRegistrationCallbackEvent callback);

    void registerInitPlayConnectionEvent(InitPlayConnectionEvent event);

    boolean isModLoaded(String... modIds);

    Path getGameDir();

    Path getConfigDir();

    boolean isDevEnv();

    Environment getEnv();

    String getControlifyVersion();

    void applyToControlifyEntrypoint(Consumer<ControlifyEntrypoint> entrypointConsumer);

    <I, O> void setupServersideHandshake(
            ResourceLocation handshakeId,
            ControlifyPacketCodec<I> serverBoundCodec,
            ControlifyPacketCodec<O> clientBoundCodec,
            Supplier<O> packetCreator,
            HandshakeCompletionEvent<I> completionEvent
    );
}
