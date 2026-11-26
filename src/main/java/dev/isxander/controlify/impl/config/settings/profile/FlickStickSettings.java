/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.settings.profile;

import dev.isxander.controlify.impl.config.dto.profile.FlickStickConfig;
import dev.isxander.controlify.impl.ingame.look.FlickSnap;

public class FlickStickSettings {
	public boolean enabled;
	/// Seconds a flick takes to complete.
	public float flickTime;
	/// Flicks within this many degrees of straight forward do nothing.
	public float forwardDeadzone;
	public FlickSnap snap;
	/// Keep turning with the stick after the flick while it stays deflected.
	public boolean rotate;

	public FlickStickSettings(boolean enabled, float flickTime, float forwardDeadzone, FlickSnap snap, boolean rotate) {
		this.enabled = enabled;
		this.flickTime = flickTime;
		this.forwardDeadzone = forwardDeadzone;
		this.snap = snap;
		this.rotate = rotate;
	}

	public static FlickStickSettings fromDTO(FlickStickConfig dto) {
		return new FlickStickSettings(dto.enabled(), dto.flickTime(), dto.forwardDeadzone(), dto.snap(), dto.rotate());
	}

	public FlickStickConfig toDTO() {
		return new FlickStickConfig(enabled, flickTime, forwardDeadzone, snap, rotate);
	}
}
