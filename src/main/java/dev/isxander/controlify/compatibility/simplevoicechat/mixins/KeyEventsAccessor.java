/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compatibility.simplevoicechat.mixins;

//? if simple_voice_chat {

import de.maxhenkel.voicechat.voice.client.KeyEvents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyEvents.class)
public interface KeyEventsAccessor {
	@Invoker
	boolean invokeCheckConnected();
}
//?}
