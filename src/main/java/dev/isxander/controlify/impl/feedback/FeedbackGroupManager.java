/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import com.google.gson.JsonParser;
import dev.isxander.controlify.impl.platform.client.resource.SimpleControlifyReloadListener;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;

import java.io.BufferedReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/// Loads `assets/controlify/controller/feedback/groups.json` from every pack:
///
/// ```json
/// {
///   "groups": {
///     "controlify:aiming": ["controlify:use_item/bow"],
///     "=mypack:ambience": ["mypack:water_tint"],
///     "-controlify:world": ["controlify:lightning"]
///   },
///   "ordering": {
///     "controlify:player": { "above": ["controlify:world"] },
///     "-controlify:gui": { "below": ["controlify:player"] },
///     "=mypack:ambience": { "below": ["controlify:world"] }
///   }
/// }
/// ```
///
/// Packs are applied from lowest to highest priority. In both sections a plain key adds,
/// `=` replaces everything lower packs said about that group, and `-` removes. Within one
/// file, replacements apply first, then removals, then additions. See [FeedbackGroups] for
/// how membership and ordering are resolved.
public final class FeedbackGroupManager implements SimpleControlifyReloadListener<FeedbackGroups> {
	private static final Identifier DEFINITIONS = CUtil.rl("controller/feedback/groups.json");

	private static final FeedbackGroups.Reporter REPORTER = new FeedbackGroups.Reporter() {
		@Override
		public void warn(String message) {
			CUtil.LOGGER.warn(message);
		}

		@Override
		public void debug(String message) {
			CUtil.LOGGER.debugLog(message);
		}
	};

	private volatile FeedbackGroups groups = FeedbackGroups.EMPTY;

	public FeedbackGroups groups() {
		return this.groups;
	}

	@Override
	public CompletableFuture<FeedbackGroups> load(ResourceManager manager, Executor executor) {
		return CompletableFuture.supplyAsync(() -> {
			List<FeedbackGroups.PackFile> files = new ArrayList<>();
			// resource stacks are ordered from lowest to highest pack priority
			for (Resource resource : manager.getResourceStack(DEFINITIONS)) {
				try (BufferedReader reader = resource.openAsReader()) {
					files.add(new FeedbackGroups.PackFile(resource.sourcePackId(), JsonParser.parseReader(reader)));
				} catch (Exception e) {
					CUtil.LOGGER.error("Failed to load feedback groups from pack '{}'", resource.sourcePackId(), e);
				}
			}
			return FeedbackGroups.resolve(files, REPORTER);
		}, executor);
	}

	@Override
	public CompletableFuture<Void> apply(FeedbackGroups data, ResourceManager manager, Executor executor) {
		return CompletableFuture.runAsync(() -> this.groups = data, executor);
	}

	@Override
	public Identifier getReloadId() {
		return CUtil.rl("feedback_groups");
	}
}
