package dev.isxander.controlify.platform.main;

import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.main.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.platform.main.events.HandshakeCompletionEvent;
import dev.isxander.controlify.platform.main.events.PlayerJoinedEvent;
import dev.isxander.controlify.platform.network.ControlifyPacketCodec;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.nio.file.Path;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PlatformMainUtil {

    private static final PlatformMainUtilImpl IMPL =
            //? if fabric
            /*new dev.isxander.controlify.platform.main.fabric.FabricPlatformMainImpl();*/
            //? if neoforge
            new dev.isxander.controlify.platform.main.neoforge.NeoforgePlatformMainImpl();

    public static void registerCommandRegistrationCallback(CommandRegistrationCallbackEvent callback) {
        IMPL.registerCommandRegistrationCallback(callback);
    }

    public static void registerPlayerJoinedEvent(PlayerJoinedEvent event) {
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

    public static <T> Supplier<T> deferredRegister(Registry<T> registry, ResourceLocation id, Supplier<? extends T> registrant) {
        return IMPL.deferredRegister(registry, id, registrant);
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
