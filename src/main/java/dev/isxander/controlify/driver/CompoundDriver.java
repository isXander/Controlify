/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.driver;

import com.google.common.collect.Lists;
import dev.isxander.controlify.controller.ControllerEntity;

import java.util.List;
import java.util.stream.Collectors;

public class CompoundDriver implements Driver {
	private final List<Driver> drivers;

	public CompoundDriver(List<Driver> drivers) {
		this.drivers = drivers;
	}

	@Override
	public void addComponents(ControllerEntity controller) {
		for (Driver driver : Lists.reverse(drivers)) {
			driver.addComponents(controller);
		}
	}

	@Override
	public void update(ControllerEntity controller, boolean outOfFocus) {
		for (Driver driver : drivers) {
			driver.update(controller, outOfFocus);
		}
	}

	@Override
	public void close() {
		for (Driver driver : drivers) {
			driver.close();
		}
	}

	@Override
	public String toString() {
		return "CompoundDriver{" +
			drivers.stream().map(d -> d.getClass().getSimpleName()).collect(Collectors.joining(",")) +
			'}';
	}
}
