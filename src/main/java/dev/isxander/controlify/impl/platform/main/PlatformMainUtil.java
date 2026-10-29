/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.platform.main;

import dev.isxander.controlify.api.ControlifyEntrypoint;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.platform.Environment;
import dev.isxander.controlify.impl.platform.main.events.CommandRegistrationCallbackEvent;
import dev.isxander.controlify.impl.platform.main.events.HandshakeCompletionEvent;
import dev.isxander.controlify.impl.platform.main.events.PlayerJoinedEvent;
import dev.isxander.controlify.impl.platform.network.C2SNetworkApi;
import dev.isxander.controlify.impl.platform.network.S2CNetworkApi;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.apache.commons.io.function.IOSupplier;

import java.io.InputStream;
import java.nio.file.Path;
import java.util.Optional;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class PlatformMainUtil {

	public static PlatformMainUtilImpl IMPL = null;
	private static boolean invokedControlifyEntrypoints;

	public static void registerCommandRegistrationCallback(CommandRegistrationCallbackEvent callback) {
		IMPL.registerCommandRegistrationCallback(callback);
	}

	public static void registerPlayerJoinedEvent(PlayerJoinedEvent event) {
		IMPL.registerInitPlayConnectionEvent(event);
	}

	public static boolean isModLoaded(String... modIds) {
		return IMPL.isModLoaded(modIds);
	}

	public static Optional<IOSupplier<InputStream>> getModFileInputStream(String modId, String path) {
		return IMPL.getModFileInputStream(modId, path);
	}

	/// Called by client bootstraps after installing both platform implementations.
	public static void invokeControlifyEntrypoints() {
		if (getEnv() != Environment.CLIENT || invokedControlifyEntrypoints) {
			return;
		}
		invokedControlifyEntrypoints = true;
		Set<Class<?>> invoked = new HashSet<>();
		applyToControlifyEntrypoint(entrypoint -> {
			if (!invoked.add(entrypoint.getClass())) {
				return;
			}
			try {
				entrypoint.onControlifyInitialize();
			} catch (Throwable throwable) {
				CUtil.LOGGER.error("Failed to initialize Controlify addon: {}", entrypoint.getClass().getName(), throwable);
			}
		});
	}

	public static void applyToControlifyEntrypoint(Consumer<ControlifyEntrypoint> entrypointConsumer) {
		IMPL.applyToControlifyEntrypoint(entrypointConsumer);
	}

	public static <I, O> void setupServersideHandshake(
			Identifier handshakeId,
			StreamCodec<FriendlyByteBuf, I> serverBoundCodec,
			StreamCodec<FriendlyByteBuf, O> clientBoundCodec,
			Supplier<O> packetCreator,
			HandshakeCompletionEvent<I> completionEvent
	) {
		IMPL.setupServersideHandshake(handshakeId, serverBoundCodec, clientBoundCodec, packetCreator, completionEvent);
	}

	public static <T> Supplier<T> deferredRegister(Registry<T> registry, Identifier id, Supplier<? extends T> registrant) {
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

	public static C2SNetworkApi c2sNetworkApi() {
		return IMPL.c2sNetworkApi();
	}

	public static S2CNetworkApi s2CNetworkApi() {
		return IMPL.s2cNetworkApi();
	}
}
