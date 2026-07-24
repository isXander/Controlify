/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.guide;

public interface GuideDomain<T extends FactCtx> {
	void registerFact(Fact<? super T> fact);

	void registerDynamicRule(Rule rule);

	GuideInstance<T> createInstance();
}
