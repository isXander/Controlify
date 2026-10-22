/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.mixins.feature.screenop.runtime;

import dev.isxander.controlify.impl.screenop.ScreenContextHolder;
import dev.isxander.controlify.impl.screenop.ScreenContextImpl;
import net.minecraft.client.gui.screens.Screen;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ScreenContextMixin implements ScreenContextHolder {
	@Unique private ScreenContextImpl<?> controlify$context;
	@Unique private boolean controlify$creatingContext;

	@Override
	public ScreenContextImpl<?> controlify$screenContext() {
		if (controlify$context == null) {
			if (controlify$creatingContext) {
				throw new IllegalStateException("Screen factory recursively resolved its own screen; use the supplied context");
			}
			controlify$creatingContext = true;
			try {
				controlify$context = new ScreenContextImpl<>((Screen) (Object) this);
			} finally {
				controlify$creatingContext = false;
			}
		}
		return controlify$context;
	}

	@Override
	public @Nullable ScreenContextImpl<?> controlify$existingScreenContext() {
		return controlify$context;
	}

	@Inject(method = "init(II)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;init()V", shift = At.Shift.AFTER))
	private void controlify$initializedWidgets(CallbackInfo ci) {
		if (controlify$context != null) {
			controlify$context.onWidgetRebuild();
		}
	}

	@Inject(method = "rebuildWidgets", at = @At("RETURN"))
	private void controlify$rebuiltWidgets(CallbackInfo ci) {
		if (controlify$context != null) {
			controlify$context.onWidgetRebuild();
		}
	}
}
