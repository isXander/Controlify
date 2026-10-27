/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import dev.isxander.controlify.impl.action.ControlifyActions;

/// A subset of Controlify's built-in actions that are API-safe;
/// meaning there is a guarantee that these will not be removed or renamed.
///
/// If you require accessing none-API-safe actions, you can do so via [ControlifyActions].
public final class BuiltinActions {
	public static final ActionAccessor GUI_PRESS = ControlifyActions.GUI_PRESS;
	public static final ActionAccessor GUI_BACK = ControlifyActions.GUI_BACK;
	public static final ActionAccessor GUI_PREV_TAB = ControlifyActions.GUI_PREV_TAB;
	public static final ActionAccessor GUI_NEXT_TAB = ControlifyActions.GUI_NEXT_TAB;
	public static final ActionAccessor GUI_ABSTRACT_ACTION_1 = ControlifyActions.GUI_ABSTRACT_ACTION_1;
	public static final ActionAccessor GUI_ABSTRACT_ACTION_2 = ControlifyActions.GUI_ABSTRACT_ACTION_2;
	public static final ActionAccessor GUI_ABSTRACT_ACTION_3 = ControlifyActions.GUI_ABSTRACT_ACTION_3;
	public static final ActionAccessor GUI_NAVI_UP = ControlifyActions.GUI_NAVI_UP;
	public static final ActionAccessor GUI_NAVI_DOWN = ControlifyActions.GUI_NAVI_DOWN;
	public static final ActionAccessor GUI_NAVI_LEFT = ControlifyActions.GUI_NAVI_LEFT;
	public static final ActionAccessor GUI_NAVI_RIGHT = ControlifyActions.GUI_NAVI_RIGHT;
	public static final ActionAccessor GUI_SECONDARY_NAVI_UP = ControlifyActions.GUI_SECONDARY_NAVI_UP;
	public static final ActionAccessor GUI_SECONDARY_NAVI_DOWN = ControlifyActions.GUI_SECONDARY_NAVI_DOWN;
	public static final ActionAccessor GUI_SECONDARY_NAVI_LEFT = ControlifyActions.GUI_SECONDARY_NAVI_LEFT;
	public static final ActionAccessor GUI_SECONDARY_NAVI_RIGHT = ControlifyActions.GUI_SECONDARY_NAVI_RIGHT;

	private BuiltinActions() {
	}
}
