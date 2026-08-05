/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gui.screen;

import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

final class RadialMenuEditModel {
	private final List<Identifier> actions;
	private @Nullable Identifier carried;

	RadialMenuEditModel(List<Identifier> actions) {
		this.actions = new ArrayList<>(new LinkedHashSet<>(actions));
	}

	List<Identifier> actions() {
		return List.copyOf(actions);
	}

	boolean isCarrying() {
		return carried != null;
	}

	@Nullable Identifier carried() {
		return carried;
	}

	boolean carriedIsEquipped() {
		return carried != null && actions.contains(carried);
	}

	void pickUp(Identifier id) {
		if (carried != null) throw new IllegalStateException("Already carrying a radial action");
		carried = id;
	}

	void drop() {
		carried = null;
	}

	int unequipCarried() {
		if (!carriedIsEquipped()) return actions.size();
		int index = actions.indexOf(carried);
		actions.remove(index);
		return index;
	}

	boolean equipCarried(int index) {
		if (carried == null || actions.contains(carried)) return false;
		actions.add(Math.clamp(index, 0, actions.size()), carried);
		return true;
	}

	boolean moveCarried(int direction) {
		if (!carriedIsEquipped() || direction == 0) return false;
		int oldIndex = actions.indexOf(carried);
		return moveCarriedTo(oldIndex + Integer.signum(direction));
	}

	boolean moveCarriedTo(int index) {
		if (!carriedIsEquipped()) return false;
		int oldIndex = actions.indexOf(carried);
		int newIndex = Math.clamp(index, 0, actions.size() - 1);
		if (oldIndex == newIndex) return false;

		actions.remove(oldIndex);
		actions.add(newIndex, carried);
		return true;
	}
}
