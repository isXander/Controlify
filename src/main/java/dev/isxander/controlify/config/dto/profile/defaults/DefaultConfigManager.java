/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.profile.defaults;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.logging.LogUtils;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import dev.isxander.controlify.config.dto.profile.ProfileConfig;
import dev.isxander.controlify.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.CrashReport;
import net.minecraft.ReportedException;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.slf4j.Logger;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class DefaultConfigManager implements SimpleControlifyReloadListener<ProfileConfig>, DefaultConfigProvider {
	public static final Identifier DEFAULT_RESOURCE = CUtil.rl("controllers/default_config.json");
	private static final Logger LOGGER = LogUtils.getLogger();

	private ProfileConfig defaultConfig;

	@Override
	public ProfileConfig getDefault() {
		if (!this.isReady()) {
			throw new IllegalStateException("Attempted to fetch default config before DefaultConfigManager was ready!");
		}
		return Objects.requireNonNull(defaultConfig);
	}

	@Override
	public boolean isReady() {
		return defaultConfig != null;
	}

	@Override
	public CompletableFuture<ProfileConfig> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			List<Resource> resources = manager.getResourceStack(DEFAULT_RESOURCE);
			if (resources.isEmpty()) {
				var report = CrashReport.forThrowable(
						new IllegalStateException("Missing " + DEFAULT_RESOURCE),
						"No default Controlify config found"
				);
				throw new ReportedException(report);
			}

			JsonObject combined = new JsonObject();
			for (Resource resource : resources) {
				try (var reader = resource.openAsReader()) {
					JsonElement json = JsonParser.parseReader(reader);
					mergeJsonObjects(combined, json.getAsJsonObject());
				} catch (Exception e) {
					LOGGER.error("Failed to read default config layer {} from pack {}", DEFAULT_RESOURCE, resource.sourcePackId(), e);
				}
			}

			DataResult<ProfileConfig> result = ProfileConfig.CODEC.parse(JsonOps.INSTANCE, combined);
			return result.result().orElseThrow(() ->
					new IllegalStateException("Failed to parse Controlify default config: " + result.error().orElse(null)));
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(ProfileConfig data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> this.defaultConfig = data, executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("default_config");
	}

	public static void mergeJsonObjects(JsonObject target, JsonObject source) {
		for (String key : source.keySet()) {
			JsonElement sourceElement = source.get(key);
			if (target.has(key) && sourceElement.isJsonObject() && target.get(key).isJsonObject()) {
				mergeJsonObjects(target.getAsJsonObject(key), sourceElement.getAsJsonObject());
			} else {
				target.add(key, sourceElement);
			}
		}
	}
}
