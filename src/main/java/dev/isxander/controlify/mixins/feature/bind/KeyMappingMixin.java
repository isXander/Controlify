/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.bindings.KeyMappingHandle;
import dev.isxander.controlify.controller.ControllerEntity;
import net.minecraft.client.KeyMapping;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

import java.util.function.BooleanSupplier;

@Mixin(KeyMapping.class)
public class KeyMappingMixin implements KeyMappingHandle {

	@Shadow
	private int clickCount;

	@Shadow
	private boolean isDown;

	@Override
	public void controlify$setPressed(boolean isDown) {
		if (isDown) {
			this.isDown = true;
			this.clickCount++;
		} else {
			this.isDown = false;
		}
	}

	@Override
	public void controlify$addToggleCondition(ControllerEntity controller, BooleanSupplier condition) {

	}

	@Unique protected void incClickCount() {
		this.clickCount++;
	}
}
