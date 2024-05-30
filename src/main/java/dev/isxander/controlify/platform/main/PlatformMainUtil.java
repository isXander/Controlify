package dev.isxander.controlify.platform.main;

import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.main.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.platform.main.events.HandshakeCompletionEvent;
import dev.isxander.controlify.platform.main.events.InitPlayConnectionEvent;
import dev.isxander.controlify.platform.main.fabric.FabricPlatformMainImpl;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PlatformMainUtil {

    private static final PlatformMainUtilImpl IMPL = new FabricPlatformMainImpl();

    public static void registerCommandRegistrationCallback(CommandRegistrationCallbackEvent callback) {
        IMPL.registerCommandRegistrationCallback(callback);
    }

    public static void registerInitPlayConnectionEvent(InitPlayConnectionEvent event) {
        IMPL.registerInitPlayConnectionEvent(event);
    }

    public static boolean isModLoaded(String... modIds) {
        return IMPL.isModLoaded(modIds);
    }

    public static void applyToControlifyEntrypoint(Consumer<ControlifyEntrypoint> entrypointConsumer) {
        IMPL.applyToControlifyEntrypoint(entrypointConsumer);
    }

    public static <I, O> void setupServersideHandshake(
            ResourceLocation handshakeId,
            ControlifyPacketCodec<I> serverBoundCodec,
            ControlifyPacketCodec<O> clientBoundCodec,
            Supplier<O> packetCreator,
            HandshakeCompletionEvent<I> completionEvent
    ) {
        IMPL.setupServersideHandshake(handshakeId, serverBoundCodec, clientBoundCodec, packetCreator, completionEvent);
    }

    public static Path getGameDir() {
        return IMPL.getGameDir();
    }

    public static Path getConfigDir() {
        return IMPL.getConfigDir();
    }

    public static boolean isDevEnv() {
        return IMPL.isDevEnv();
    }

    public static Environment getEnv() {
        return IMPL.getEnv();
    }

    public static String getControlifyVersion() {
        return IMPL.getControlifyVersion();
    }
}
