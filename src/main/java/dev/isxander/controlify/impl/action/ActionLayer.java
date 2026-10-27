/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action;

import dev.isxander.controlify.api.action.ActionLayerMode;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;

import java.util.function.Predicate;

/// Internal layer definition; the public API exposes its identifier and registration parameters only.
public record ActionLayer(Identifier id, ActionLayerMode mode, Predicate<Minecraft> isActive) {
}
