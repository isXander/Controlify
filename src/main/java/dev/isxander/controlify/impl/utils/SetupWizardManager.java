/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.utils;

import dev.isxander.controlify.impl.gui.screen.DontInterruptScreen;

import java.util.ArrayDeque;
import java.util.Queue;

public class SetupWizardManager {
	private final Queue<SetupWizard> setupWizards = new ArrayDeque<>();
	private SetupWizard currentSetupWizard = null;

	public void push(SetupWizard setupWizard) {
		this.setupWizards.add(setupWizard);
	}

	public void tick() {
		if (MinecraftUtil.getOverlay() == null) {
			if (this.currentSetupWizard != null && this.currentSetupWizard.isDone()) {
				this.currentSetupWizard = null;
			}

			if (!this.setupWizards.isEmpty() && !(MinecraftUtil.getScreen() instanceof DontInterruptScreen)) {
				this.currentSetupWizard = this.setupWizards.poll();
				MinecraftUtil.setScreen(this.currentSetupWizard.start(MinecraftUtil.getScreen()));
			}
		}
	}
}
