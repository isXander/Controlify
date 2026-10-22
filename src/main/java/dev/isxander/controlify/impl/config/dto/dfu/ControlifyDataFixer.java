/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.config.dto.dfu;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import dev.isxander.controlify.impl.config.dto.dfu.fixes.*;
import dev.isxander.controlify.impl.config.settings.GlobalSettings;
import dev.isxander.controlify.impl.config.settings.profile.ProfileSettings;

public final class ControlifyDataFixer {
	public static final int CURRENT_VERSION = 10;

	public static DataFixer getFixer() {
		return ProductionFixerHolder.FIXER;
	}

	public static DataFixer createFixer(ProfileSettings profileDefaults) {
		return createFixer(GlobalSettings.defaults(), profileDefaults);
	}

	private static DataFixer createFixer() {
		return createFixer(GlobalSettings.defaults(), ProfileSettings.createDefault());
	}

	private static DataFixer createFixer(GlobalSettings globalDefaults, ProfileSettings profileDefaults) {
		var builder = new DataFixerBuilder(CURRENT_VERSION);

		var v0 = builder.addSchema(0, ControlifySchemas.V0::new);

		var v1 = builder.addSchema(1, ControlifySchemas.V1::new);
		builder.addFixer(new TheHolyMigrationFix(v1, globalDefaults, profileDefaults));

		var v2 = builder.addSchema(2, ControlifySchemas.V2::new);
		builder.addFixer(new AnalogueMovementWhitelistFix(v2));
		builder.addFixer(new HorizontalLookInvertFix(v2));

		var v3 = builder.addSchema(3, ControlifySchemas.V3::new);
		var v6 = builder.addSchema(6, ControlifySchemas.V6::new);
		builder.addFixer(new DualsenseConfigFix(v6, profileDefaults));

		// v7
		var v7 = builder.addSchema(7, ControlifySchemas.V7::new);
		builder.addFixer(new GuideGuiScaleFix(v7, profileDefaults));

		// v8
		var v8 = builder.addSchema(8, ControlifySchemas.V8::new);
		builder.addFixer(new GeneratedBindingIdsFix(v8));
		builder.addFixer(new RadialMenuActionsFix(v8));

		var v9 = builder.addSchema(9, ControlifySchemas.V9::new);
		builder.addFixer(new GyroLowPassFix(v9, profileDefaults));

		var v10 = builder.addSchema(10, ControlifySchemas.V10::new);
		builder.addFixer(new RemoveRelativeGyroFix(v10));

		return builder.build().fixer();
	}

	private ControlifyDataFixer() {
	}

	private static final class ProductionFixerHolder {
		private static final DataFixer FIXER = createFixer();
	}
}
