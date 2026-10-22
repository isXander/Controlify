/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controllermanager;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.hid.HIDID;
import dev.isxander.controlify.impl.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.impl.utils.CUtil;
import dev.isxander.controlify.impl.utils.JsonTreeParser;
import net.minecraft.util.Util;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.quiltmc.parsers.json.JsonReader;

import java.io.BufferedReader;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

public class ControllerTypeManager implements SimpleControlifyReloadListener<ControllerTypeManager.Preparations> {

	private Map<HIDID, ControllerType> typeMap = new HashMap<>();

	public static final Identifier ID = CUtil.rl("controller_type");

	private static final Codec<ControllerTypeEntry> ENTRY_CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.list(HIDID.CODEC)
					.comapFlatMap(list -> list.isEmpty() ? DataResult.error(() -> "At least one HID must be present") : DataResult.success(list), list -> list)
					.fieldOf("hids")
					.forGetter(ControllerTypeEntry::hid),
			ControllerType.CODEC.forGetter(ControllerTypeEntry::type)
	).apply(instance, ControllerTypeEntry::new));

	public ControllerType getControllerType(HIDID hid) {
		return typeMap.getOrDefault(hid, ControllerType.DEFAULT);
	}

	public Map<HIDID, ControllerType> getTypeMap() {
		return typeMap;
	}

	@Override
	public CompletableFuture<Preparations> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> manager.getResourceStack(CUtil.rl("controller/controller_identification.json5")), executor)
				.thenCompose(resources -> {
					List<CompletableFuture<List<Map.Entry<HIDID, ControllerType>>>> futures = new ArrayList<>();
					for (Resource resource : resources) {
						futures.add(CompletableFuture.supplyAsync(() -> readIdentificationResource(resource), executor));
					}

					return Util.sequence(futures)
							.thenApply(listOfEntries -> listOfEntries.stream()
									.flatMap(List::stream)
									.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (a, b) -> b)));

				})
				.thenApply(Preparations::new);
	}

	private List<Map.Entry<HIDID, ControllerType>> readIdentificationResource(Resource resource) {
		Map<HIDID, ControllerType> typeMap = new HashMap<>();

		try (BufferedReader resourceReader = resource.openAsReader()) {
			var reader = JsonReader.json5(resourceReader);
			JsonElement json = JsonTreeParser.parse(reader);

			ENTRY_CODEC.listOf().parse(JsonOps.INSTANCE, json)
					.resultOrPartial(CUtil.LOGGER::error)
					.ifPresent(entries -> {
						for (var entry : entries) {
							for (var hid : entry.hid()) {
								typeMap.put(hid, entry.type());
							}
						}
					});
		} catch (Exception e) {
			CUtil.LOGGER.error("Failed to read controller identification database!", e);
		}

		return typeMap.entrySet().stream().toList();
	}

	@Override
	public CompletableFuture<Void> apply(Preparations data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> {
			this.typeMap = data.typeMap();
			triggerFullTypeReload();
		}, executor);
	}

	public void triggerFullTypeReload() {
		Controlify.instance().getControllerCoordinator().ifPresent(coordinator -> {
			// Reinitialization replaces entries in the manager's live collection.
			for (ControllerImpl controller : List.copyOf(coordinator.controllerManager().getAllControllers())) {
				reloadTypeForController(coordinator, controller);
			}
		});
	}

	public void reloadTypeForController(ControllerCoordinator coordinator, ControllerImpl controller) {
		ControllerType newType = this.getControllerType(controller.info().hid());
		if (!newType.equals(controller.info().type())) {
			coordinator.reinitController(controller, newType);
		}
	}

	@Override
	public Identifier getReloadId() {
		return ID;
	}


	public record Preparations(Map<HIDID, ControllerType> typeMap) {}

	private record ControllerTypeEntry(List<HIDID> hid, ControllerType type) {}
}
