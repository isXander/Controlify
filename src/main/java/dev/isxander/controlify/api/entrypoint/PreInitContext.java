/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.entrypoint;

import dev.isxander.controlify.api.bind.ControlifyBindApi;
import dev.isxander.controlify.api.guide.GuideDomainRegistry;

public interface PreInitContext {
	ControlifyBindApi bindings();

	GuideDomainRegistry guides();
}
