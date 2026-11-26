/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.haptic;

import dev.isxander.controlify.impl.config.settings.profile.HDHapticSettings;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.feedback.PcmSink;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

/// Present on controllers whose haptic actuators are driven by an audio stream, such as the
/// DualSense. The feedback engine's `PcmRenderer` synthesises into [#sink()].
public class PcmHapticsComponent extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("pcm_haptics");

	private final PcmSink sink;

	public PcmHapticsComponent(PcmSink sink) {
		super(ID);
		this.sink = sink;
	}

	public PcmSink sink() {
		return this.sink;
	}

	public HDHapticSettings settings() {
		return this.controller().settings().hdHaptic;
	}

	public HDHapticSettings defaultSettings() {
		return this.controller().defaultSettings().hdHaptic;
	}
}
