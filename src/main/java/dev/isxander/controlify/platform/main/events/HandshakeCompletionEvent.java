/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.platform.main.events;

import net.minecraft.server.network.ServerLoginPacketListenerImpl;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface HandshakeCompletionEvent<I> {
	void onCompletion(@Nullable I packet, ServerLoginPacketListenerImpl handler);
}
