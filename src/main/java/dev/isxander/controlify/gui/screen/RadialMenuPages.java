/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.gui.screen;

import java.util.ArrayList;
import java.util.List;

final class RadialMenuPages {
	static final int PAGE_SIZE = 8;

	static <T> List<List<T>> partition(List<T> items) {
		if (items.isEmpty()) return List.of(List.of());

		List<List<T>> pages = new ArrayList<>((items.size() + PAGE_SIZE - 1) / PAGE_SIZE);
		for (int start = 0; start < items.size(); start += PAGE_SIZE) {
			pages.add(List.copyOf(items.subList(start, Math.min(start + PAGE_SIZE, items.size()))));
		}
		return List.copyOf(pages);
	}

	private RadialMenuPages() {
	}
}
