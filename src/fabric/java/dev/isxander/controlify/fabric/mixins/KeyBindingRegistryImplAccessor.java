/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.fabric.mixins;

import net.fabricmc.fabric.impl.client.keymapping.KeyMappingRegistryImpl;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@SuppressWarnings("UnstableApiUsage")
@Mixin(KeyMappingRegistryImpl.class)
public interface KeyBindingRegistryImplAccessor {
	@Accessor("MODDED_KEY_BINDINGS")
	static List<KeyMapping> getCustomKeys() {
		throw new AssertionError();
	}
}
