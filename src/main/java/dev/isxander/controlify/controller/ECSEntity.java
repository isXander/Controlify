/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller;

import net.minecraft.resources.Identifier;

import java.util.Map;
import java.util.Optional;

public interface ECSEntity {
	Map<Identifier, ECSComponent> getAllComponents();

	<T extends ECSComponent> boolean setComponent(T component);

	boolean removeComponent(Identifier id);

	<T extends ECSComponent> Optional<T> getComponent(Identifier id);
}
