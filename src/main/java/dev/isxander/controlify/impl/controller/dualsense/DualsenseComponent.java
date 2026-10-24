/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.controller.dualsense;

import dev.isxander.controlify.impl.config.settings.profile.DualsenseSettings;
import dev.isxander.controlify.impl.controller.ControllerComponent;
import dev.isxander.controlify.impl.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class DualsenseComponent extends ControllerComponent {
	public static final Identifier ID = CUtil.rl("dualsense");

	private boolean muteLight;

	private DualsenseTriggerEffect leftTriggerEffect;
	private DualsenseTriggerEffect rightTriggerEffect;

	private boolean dirty;

	public DualsenseComponent() {
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

	public void setMuteLight(boolean on) {
		if (this.muteLight != on) {
			this.muteLight = on;
			this.setDirty();
		}
	}

	public boolean getMuteLight() {
		return this.muteLight;
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
