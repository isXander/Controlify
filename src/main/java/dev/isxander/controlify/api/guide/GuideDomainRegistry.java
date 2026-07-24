/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.guide;

import net.minecraft.resources.Identifier;

public interface GuideDomainRegistry {
	GuideDomain<InGameCtx> inGame();

	GuideDomain<ContainerCtx> container();

	<T extends FactCtx> GuideDomain<T> registerCustom(Identifier domainId);
}
