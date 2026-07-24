/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.controllermanager;

/**
 * A unique, abstract identifier for a controller.
 * Each implementation of {@link ControllerManager} will have a different implementation.
 */
public interface UniqueControllerID {
	@Override
	boolean equals(Object obj);

	@Override
	String toString();

	@Override
	int hashCode();
}
