/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.driver;

import com.google.common.collect.Lists;
import dev.isxander.controlify.impl.controller.ControllerImpl;

import java.util.List;
import java.util.stream.Collectors;

public class CompoundDriver implements Driver {
	private final List<Driver> drivers;

	public CompoundDriver(List<Driver> drivers) {
		this.drivers = drivers;
	}

	@Override
	public void addComponents(ControllerImpl controller) {
		for (Driver driver : Lists.reverse(drivers)) {
			driver.addComponents(controller);
		}
	}

	@Override
	public void update(ControllerImpl controller) {
		for (Driver driver : drivers) {
			driver.update(controller);
		}
	}

	@Override
	public void setActive(boolean active) {
		for (Driver driver : drivers) {
			driver.setActive(active);
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
