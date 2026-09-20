/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.compatibility.iris.screenop;

//? if iris {

import dev.isxander.controlify.screenop.compat.AbstractSliderComponentProcessor;

import java.util.function.Consumer;

public class BaseOptionElementComponentProcessor extends AbstractSliderComponentProcessor {
	private final Consumer<Boolean> cycleMethod;

	public BaseOptionElementComponentProcessor(Consumer<Boolean> cycleMethod) {
		this.cycleMethod = cycleMethod;
	}

	@Override
	protected void incrementSlider(boolean reverse) {
		this.cycleMethod.accept(reverse);
	}
}
//?}
