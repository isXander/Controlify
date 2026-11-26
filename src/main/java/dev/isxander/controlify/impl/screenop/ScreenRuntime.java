/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.screenop;

import dev.isxander.controlify.impl.Controlify;
import dev.isxander.controlify.impl.utils.MinecraftUtil;
import dev.isxander.controlify.mixins.feature.virtualmouse.MouseHandlerAccessor;
import net.minecraft.client.Minecraft;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/// Host integration only: activates sidecars and dispatches frames. Cursor input
/// and rendering live in the registered screen behaviours.
public final class ScreenRuntime {
	private static @Nullable ScreenContextImpl<?> active;
	private static boolean changing;

	private ScreenRuntime() {
	}

	public static @Nullable ScreenContextImpl<?> current() {
		if (!ScreenBehaviourRegistryImpl.INSTANCE.isReady()) {
			return null;
		}
		if ((active == null ? null : active.screen()) != MinecraftUtil.getScreen()) {
			onScreenChanged();
		}
		return active;
	}

	public static Optional<VirtualMouseState> virtualMouse() {
		return Optional.ofNullable(current()).map(ScreenContextImpl::virtualMouse);
	}

	public static void onScreenChanged() {
		if (changing || !ScreenBehaviourRegistryImpl.INSTANCE.isReady()) {
			return;
		}
		if (active != null && active.screen() == MinecraftUtil.getScreen()) {
			return;
		}
		changing = true;
		try {
			var previous = active;
			active = null;
			boolean hadCursor = previous != null && previous.virtualMouse().isEnabled();
			if (previous != null) {
				previous.onScreenActiveChanged(false);
			}
			var screen = MinecraftUtil.getScreen();
			if (screen != null) {
				active = ScreenContextImpl.get(screen);
				active.onScreenActiveChanged(true);
				if (Controlify.instance().currentInputMode().isController()) {
					Controlify.instance().hideMouse(true, !active.virtualMouse().isEnabled());
				}
			} else if (hadCursor) {
				var mouse = Minecraft.getInstance().mouseHandler;
				((MouseHandlerAccessor) mouse).controlify$setMouseGrabbed(false);
				mouse.grabMouse();
			}
		} finally {
			changing = false;
		}
		if ((active == null ? null : active.screen()) != MinecraftUtil.getScreen()) {
			onScreenChanged();
		}
	}

	public static void update() {
		var context = current();
		if (context != null) {
			context.update(Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks());
		}
	}
}
