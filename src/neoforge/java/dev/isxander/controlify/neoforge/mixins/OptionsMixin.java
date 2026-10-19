/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.neoforge.mixins;

import dev.isxander.controlify.neoforge.platform.VanillaKeyMappingHolder;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Arrays;

@Mixin(Options.class)
public class OptionsMixin implements VanillaKeyMappingHolder {
	@Shadow public KeyMapping[] keyMappings;

	@Unique private KeyMapping[] controlify$vanillaKeyMappings;

	// Capture the vanilla key mappings as soon as the vanilla array is assigned.
	// NeoForge appends modded key mappings to this field inside the constructor,
	// so capturing at RETURN would make the "vanilla" snapshot include modded keys
	// and the modded-key diff in NeoforgePlatformClientImpl would always be empty.
	@Inject(
			method = "<init>",
			at = @At(
					value = "FIELD",
					target = "Lnet/minecraft/client/Options;keyMappings:[Lnet/minecraft/client/KeyMapping;",
					opcode = Opcodes.PUTFIELD,
					shift = At.Shift.AFTER
			)
	)
	private void storeVanillaKeybindsBeforeModification(CallbackInfo ci) {
		this.controlify$vanillaKeyMappings = Arrays.copyOf(keyMappings, keyMappings.length);
	}

	@Override
	public KeyMapping[] controlify$getVanillaKeys() {
		return controlify$vanillaKeyMappings;
	}
}
