package dev.isxander.controlify.fabric.platform;

import dev.isxander.controlify.api.entrypoint.ControlifyEntrypoint;
import dev.isxander.controlify.fabric.platform.network.C2SNetworkApiFabric;
import dev.isxander.controlify.fabric.platform.network.S2CNetworkApiFabric;
import dev.isxander.controlify.platform.Environment;
import dev.isxander.controlify.platform.main.PlatformMainUtilImpl;
import dev.isxander.controlify.platform.main.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.platform.main.events.HandshakeCompletionEvent;
import dev.isxander.controlify.platform.main.events.PlayerJoinedEvent;
import dev.isxander.controlify.platform.network.C2SNetworkApi;
import dev.isxander.controlify.platform.network.S2CNetworkApi;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.FriendlyByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.apache.commons.io.function.IOSupplier;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class FabricPlatformMainImpl implements PlatformMainUtilImpl {
	private static final C2SNetworkApi c2sNetworkApi = new C2SNetworkApiFabric();
	private static final S2CNetworkApi s2cNetworkApi = new S2CNetworkApiFabric();

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
        // Use both Fabric's entrypoint system and ServiceLoader to maximize compatibility
        FabricLoader.getInstance().getEntrypoints("controlify", ControlifyEntrypoint.class).forEach(entrypointConsumer);
        ServiceLoader.load(ControlifyEntrypoint.class).forEach(entrypointConsumer);
    }

    @Override
    public <I, O> void setupServersideHandshake(Identifier handshakeId, StreamCodec<FriendlyByteBuf, I> serverBoundCodec, StreamCodec<FriendlyByteBuf, O> clientBoundCodec, Supplier<O> packetCreator, HandshakeCompletionEvent<I> completionEvent) {
        ServerLoginConnectionEvents.QUERY_START.register((handler, server, sender, synchronizer) -> {
            O decodedPacket = packetCreator.get();

            FriendlyByteBuf encodedPacket = FriendlyByteBufs.create();
            clientBoundCodec.encode(encodedPacket, decodedPacket);

            sender.sendPacket(handshakeId, encodedPacket);
        });

        ServerLoginNetworking.registerGlobalReceiver(handshakeId, (server, handler, understood, buf, synchronizer, responseSender) -> {
            I decodedPacket = understood ? serverBoundCodec.decode(buf) : null;

            completionEvent.onCompletion(decodedPacket, handler);
        });
    }

    @Override
    public <T> Supplier<T> deferredRegister(Registry<T> registry, Identifier id, Supplier<? extends T> registrant) {
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

	@Override
	public C2SNetworkApi c2sNetworkApi() {
		return c2sNetworkApi;
	}

	@Override
	public S2CNetworkApi s2cNetworkApi() {
		return s2cNetworkApi;
	}

	@Override
	public Optional<IOSupplier<InputStream>> getModFileInputStream(String modId, String path) {
		return FabricLoader.getInstance().getModContainer(modId)
			.flatMap(mod -> mod.findPath(path))
			.map(virtPath -> () -> Files.newInputStream(virtPath));
	}
}
