/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compat.yacl.screenop;

import dev.isxander.controlify.api.action.BuiltinActions;
import dev.isxander.controlify.api.screenop.*;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.compat.yacl.mixins.GroupSeparatorEntryAccessor;
import dev.isxander.yacl3.gui.OptionListWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OptionListWidgetBehaviour implements ElementBehaviour {
	private final OptionListWidget optionListWidget;

	public OptionListWidgetBehaviour(OptionListWidget optionListWidget) {
		this.optionListWidget = optionListWidget;
	}

	/**
	 * Allows navigating between groups using secondary navigation up/down.
	 */
	@Override
	public void handleNavigation(ScreenContext<?> screen, Controller controller, Runnable next) {
		boolean handled = false;
		while (BuiltinActions.GUI_SECONDARY_NAVI_UP.on(controller).pulse().consumePulse()) {
			handled |= moveGroup(this.optionListWidget, -1);
		}
		while (BuiltinActions.GUI_SECONDARY_NAVI_DOWN.on(controller).pulse().consumePulse()) {
			handled |= moveGroup(this.optionListWidget, 1);
		}
		if (!handled) {
			next.run();
		}
	}

	private static boolean moveGroup(OptionListWidget optionList, int direction) {
		OptionListWidget.GroupSeparatorEntry nextGroup = findNextGroup(optionList, direction);
		if (nextGroup != null) {
			if (nextGroup.isExpanded()) {
				var childEntries = ((GroupSeparatorEntryAccessor) nextGroup).getChildEntries();
				if (!childEntries.isEmpty()) {
					var childEntry = childEntries.get(0);
					var buttonWithinChild = childEntry.children().get(0);
					optionList.setFocused(childEntries.get(0));
					childEntry.setFocused(buttonWithinChild);
				} else {
					optionList.setFocused(nextGroup);
				}
			} else {
				optionList.setFocused(nextGroup);
			}
			return true;
		}
		return false;
	}

	private static @Nullable OptionListWidget.GroupSeparatorEntry findNextGroup(OptionListWidget optionList, int direction) {
		List<OptionListWidget.Entry> entries = optionList.children();
		OptionListWidget.Entry focusedEntry = optionList.getFocused();
		int currentIndex = entries.indexOf(focusedEntry);
		if (direction < 0) {
			currentIndex -= 2;
		}
		if (currentIndex < 0) {
			currentIndex = 0;
		}

		for (int i = currentIndex; i >= 0 && i < entries.size(); i += direction) {
			OptionListWidget.Entry entry = entries.get(i);
			if (entry instanceof OptionListWidget.GroupSeparatorEntry groupEntry) {
				return groupEntry;
			}
		}

		return null;
	}
}
