/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gametest.framework;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.config.settings.GlobalSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.controller.ControllerEntity;
import dev.isxander.controlify.controller.gyro.GyroState;
import dev.isxander.controlify.controllermanager.ControllerManager;
import dev.isxander.controlify.gametest.mixin.ControlifySettingsAccessor;
import dev.isxander.controlify.gametest.mixin.ToastInstanceAccessor;
import dev.isxander.controlify.gametest.mixin.ToastManagerAccessor;
import dev.isxander.controlify.utils.MinecraftUtil;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.fabricmc.fabric.api.resource.v1.ResourceLoader;
import net.fabricmc.fabric.api.resource.v1.pack.PackActivationType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("UnstableApiUsage")
public final class CTestUtil {

	private CTestUtil() {
	}

	public static Identifier id(String path) {
		return Identifier.fromNamespaceAndPath("controlify_test", path);
	}

	public static void registerBuiltinResourcePack(Identifier packId) {
		ResourceLoader.registerBuiltinPack(
			packId,
			FabricLoader.getInstance().getModContainer("controlify_test").orElseThrow(),
			PackActivationType.NORMAL
		);
	}

	public static ReversibleCallback applyResourcePack(ClientGameTestContext context, Identifier packId) {
		return applyResourcePack(context, packId.toString());
	}

	public static ReversibleCallback applyResourcePack(ClientGameTestContext context, String packId) {
		updatePackRepo(context, packRepo -> packRepo.addPack(packId));

		return () -> {
			updatePackRepo(context, packRepo -> packRepo.removePack(packId));
		};
	}

	private static void updatePackRepo(ClientGameTestContext context, Consumer<PackRepository> updater) {
		context.runOnClient(minecraft -> {
			PackRepository packRepo = minecraft.getResourcePackRepository();
			updater.accept(packRepo);
			// this initiates reload
			minecraft.options.updateResourcePacks(packRepo);
		});

		// this initiates reload too, but if one is already occurring, it attaches to the current one
		reloadResources(context);
	}

	public static void reloadResources(ClientGameTestContext context) {
		CompletableFuture<Void> reloadInstance = context.computeOnClient(minecraft -> {
			var future = minecraft.reloadResourcePacks();
			if (MinecraftUtil.getOverlay() instanceof LoadingOverlay) {
				return future;
			} else {
				throw new IllegalStateException("Reload did not initiate");
			}
		});

		// may take a while on a CI
		context.waitFor(_ -> reloadInstance.isDone(), SharedConstants.TICKS_PER_SECOND * 30);
		if (reloadInstance.isCompletedExceptionally()) {
			throw new IllegalStateException("The reload failed", reloadInstance.exceptionNow());
		}
		waitForOverlay(context, null);
	}

	public static ReversibleCallback setLanguage(ClientGameTestContext context, String code) {
		String previousLanguageCode = context.computeOnClient(minecraft -> {
			String previous = minecraft.getLanguageManager().getSelected();
			minecraft.getLanguageManager().setSelected(code);
			minecraft.options.languageCode = code;
			return previous;
		});

		reloadResources(context);

		return () -> {
			context.runOnClient(minecraft -> {
				minecraft.getLanguageManager().setSelected(previousLanguageCode);
				minecraft.options.languageCode = previousLanguageCode;
			});

			reloadResources(context);
		};
	}

	public static int waitForOverlay(ClientGameTestContext context, @Nullable Class<? extends Overlay> overlayClass) {
		if (overlayClass == null) {
			return context.waitFor(_ -> MinecraftUtil.getOverlay() == null);
		} else {
			return context.waitFor(_ -> overlayClass.isInstance(MinecraftUtil.getOverlay()));
		}
	}

	public static ServerPlayer getPrincipalPlayer(MinecraftServer server) {
		// fabric gametest always calls player Player0
		return server.getPlayerList().getPlayer("Player0");
	}

	public static <T extends Entity> T summonEntityAtPlayer(MinecraftServer server, EntityType<T> entityType, Consumer<T> preAdd) {
		ServerPlayer player = getPrincipalPlayer(server);
		ServerLevel level = player.level();

		T entity = entityType.create(level, EntitySpawnReason.COMMAND);
		entity.teleportTo(player.getX(), player.getY(), player.getZ());
		preAdd.accept(entity);
		level.addFreshEntity(entity);
		return entity;
	}

	public static <T extends Entity> T summonEntityAtPlayer(MinecraftServer server, EntityType<T> entityType) {
		return summonEntityAtPlayer(server, entityType, _ -> {});
	}

	public static void clearToasts(ClientGameTestContext context) {
		context.runOnClient(minecraft -> {
			//? if >=26.2 {
			minecraft.gui.toastManager().clear();
			//?} else {
			/*minecraft.getToastManager().clear();
			*///?}
		});
	}

	public static boolean isToastPresent(Minecraft minecraft, String translationKey) {
		//? if >=26.2 {
		var toastManager = minecraft.gui.toastManager();
		//?} else {
		/*var toastManager = minecraft.getToastManager();
		*///?}

		String text = Component.translatable(translationKey).getString();

		var toastManagerAccessor = (ToastManagerAccessor) toastManager;
		return Stream.concat(
			toastManagerAccessor.controlify_test$getQueued().stream(),
			toastManagerAccessor.controlify_test$getVisibleToasts().stream()
				.map(instance -> ((ToastInstanceAccessor) instance).controlify_test$getToast())
			)
			.flatMap(toast -> toast instanceof SystemToastDuck duck ? Stream.of(duck) : Stream.empty())
			.flatMap(duck -> Stream.of(duck.controlify_test$getMessage(), duck.controlify_test$getTitle()))
			.anyMatch(component -> text.equals(component.getString()));
	}
}
