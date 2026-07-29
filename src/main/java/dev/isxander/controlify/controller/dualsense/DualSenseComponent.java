/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controller.dualsense;

import dev.isxander.controlify.config.settings.profile.DualSenseSettings;
import dev.isxander.controlify.controller.impl.ECSComponentImpl;
import dev.isxander.controlify.driver.dualsense.DualsenseTriggerEffect;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

import java.util.Objects;

public class DualSenseComponent extends ECSComponentImpl {
	public static final Identifier ID = CUtil.rl("dualsense");

	private boolean muteLight;

	private DualsenseTriggerEffect leftTriggerEffect;
	private DualsenseTriggerEffect rightTriggerEffect;

	private boolean dirty;

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

	public DualSenseSettings settings() {
		return this.controller().settings().dualsense;
	}

	public DualSenseSettings defaultSettings() {
		return this.controller().defaultSettings().dualsense;
	}

	@Override
	public Identifier id() {
		return ID;
	}
}
