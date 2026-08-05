/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.dfu;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import dev.isxander.controlify.config.dto.dfu.fixes.*;
import dev.isxander.controlify.config.settings.GlobalSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;

public final class ControlifyDataFixer {
	public static final int CURRENT_VERSION = 8;

	private static final DataFixer FIXER = createFixer();

	public static DataFixer getFixer() {
		return FIXER;
	}

	private static DataFixer createFixer() {
		var builder = new DataFixerBuilder(CURRENT_VERSION);

		var v0 = builder.addSchema(0, ControlifySchemas.V0::new);
		var v1 = builder.addSchema(1, ControlifySchemas.V1::new);
		var v2 = builder.addSchema(2, ControlifySchemas.V2::new);
		var v3 = builder.addSchema(3, ControlifySchemas.V3::new);
		var v6 = builder.addSchema(6, ControlifySchemas.V6::new);
		var v7 = builder.addSchema(7, ControlifySchemas.V7::new);
		var v8 = builder.addSchema(8, ControlifySchemas.V8::new);

		var globalDefaults = GlobalSettings.defaults();
		var profileDefaults = ProfileSettings.createDefault();

		// v1
		builder.addFixer(new TheHolyMigrationFix(v1, globalDefaults, profileDefaults));

		// v2
		builder.addFixer(new AnalogueMovementWhitelistFix(v2));
		builder.addFixer(new HorizontalLookInvertFix(v2));

		// v6
		builder.addFixer(new DualsenseConfigFix(v6, profileDefaults));

		// v7
		builder.addFixer(new GuideGuiScaleFix(v7, profileDefaults));

		// v8
		builder.addFixer(new GeneratedBindingIdsFix(v8));
		builder.addFixer(new RadialMenuActionsFix(v8));

		return builder.build().fixer();
	}

	private ControlifyDataFixer() {
	}
}
