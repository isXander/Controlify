/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.ServiceConfigurationError;
import java.util.ServiceLoader;

public final class ServiceUtil {
	private static final int MAX_ERRORS = 64;

	private ServiceUtil() {
	}

	/// Loads every service provider that can be instantiated.
	/// A malformed services entry, or a provider that fails to load, is logged and skipped
	/// instead of stopping discovery of the others.
	public static <S> List<S> loadAll(Class<S> service) {
		List<S> providers = new ArrayList<>();
		var iterator = ServiceLoader.load(service).iterator();
		int errors = 0;
		while (errors < MAX_ERRORS) {
			try {
				if (!iterator.hasNext()) {
					break;
				}
				providers.add(iterator.next());
			} catch (ServiceConfigurationError | RuntimeException e) {
				errors++;
				CUtil.LOGGER.error("Skipping a malformed or failing {} service provider", e, service.getSimpleName());
			}
		}
		return providers;
	}
}
