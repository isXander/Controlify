/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.dualsense;

import dev.isxander.controlify.api.controller.dualsense.DualsenseComponent;
import dev.isxander.controlify.api.controller.dualsense.MicLightMode;
import dev.isxander.controlify.impl.config.settings.profile.DualsenseSettings;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Objects;

public class DualsenseComponentImpl extends ControllerComponent implements DualsenseComponent {
	public static final Identifier ID = CUtil.rl("dualsense");

	private MicLightMode micLight = MicLightMode.OFF;

	private @Nullable DualsenseTriggerEffect leftTriggerEffect;
	private @Nullable DualsenseTriggerEffect rightTriggerEffect;

	private boolean dirty;

	public DualsenseComponentImpl() {
		super(ID);
	}

	public void setLeftTriggerEffect(DualsenseTriggerEffect effect) {
		if (!Objects.equals(effect, this.leftTriggerEffect)) {
			this.setDirty();
		}
		this.leftTriggerEffect = effect;
	}

	public DualsenseTriggerEffect getLeftTriggerEffect() {
		return this.leftTriggerEffect;
	}

	public void setRightTriggerEffect(DualsenseTriggerEffect effect) {
		if (!Objects.equals(effect, this.rightTriggerEffect)) {
			this.setDirty();
		}
		this.rightTriggerEffect = effect;
	}

	public DualsenseTriggerEffect getRightTriggerEffect() {
		return this.rightTriggerEffect;
	}

	@Override
	public void setMicLightMode(@NonNull MicLightMode mode) {
		if (this.micLight != mode) {
			this.micLight = mode;
			this.setDirty();
		}
	}

	public MicLightMode getMicLightMode() {
		return this.micLight;
	}

	private void setDirty() {
		this.dirty = true;
	}

	public boolean consumeDirty() {
		boolean old = this.dirty;
		this.dirty = false;
		return old;
	}

	public DualsenseSettings settings() {
		return this.controller().settings().dualsense;
	}

	public DualsenseSettings defaultSettings() {
		return this.controller().defaultSettings().dualsense;
	}
}
