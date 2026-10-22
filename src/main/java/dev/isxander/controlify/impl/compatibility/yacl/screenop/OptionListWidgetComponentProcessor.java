/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.compatibility.yacl.screenop;

import dev.isxander.controlify.impl.action.ControlifyActions;
import dev.isxander.controlify.impl.compatibility.yacl.mixins.GroupSeparatorEntryAccessor;
import dev.isxander.controlify.impl.controller.ControllerImpl;
import dev.isxander.controlify.impl.screenop.ComponentProcessor;
import dev.isxander.controlify.impl.screenop.ScreenProcessor;
import dev.isxander.yacl3.gui.OptionListWidget;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class OptionListWidgetComponentProcessor implements ComponentProcessor {
	private final OptionListWidget optionListWidget;

	public OptionListWidgetComponentProcessor(OptionListWidget optionListWidget) {
		this.optionListWidget = optionListWidget;
	}

	/**
	 * Allows navigating between groups using secondary navigation up/down.
	 */
	@Override
	public boolean overrideControllerNavigation(ScreenProcessor<?> screen, ControllerImpl controller) {
		boolean up = ControlifyActions.GUI_SECONDARY_NAVI_UP.on(controller).pulse().consumePulse();
		boolean down = ControlifyActions.GUI_SECONDARY_NAVI_DOWN.on(controller).pulse().consumePulse();
		if (up || down) {
			OptionListWidget.GroupSeparatorEntry nextGroup = findNextGroup(optionListWidget, up ? -1 : 1);
			if (nextGroup != null) {
				if (nextGroup.isExpanded()) {
					var childEntries = ((GroupSeparatorEntryAccessor) nextGroup).getChildEntries();
					if (!childEntries.isEmpty()) {
						var childEntry = childEntries.get(0);
						var buttonWithinChild = childEntry.children().get(0);
						optionListWidget.setFocused(childEntries.get(0));
						childEntry.setFocused(buttonWithinChild);
					} else {
						optionListWidget.setFocused(nextGroup);
					}
				} else {
					optionListWidget.setFocused(nextGroup);
				}
				return true;
			}
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
