/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.config.dto.dfu;

import com.mojang.datafixers.DataFixer;
import com.mojang.datafixers.DataFixerBuilder;
import dev.isxander.controlify.config.dto.dfu.fixes.AnalogueMovementWhitelistFix;
import dev.isxander.controlify.config.dto.dfu.fixes.TheHolyMigrationFix;
import dev.isxander.controlify.config.settings.GlobalSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.utils.CUtil;

public final class ControlifyDataFixer {
	public static final int CURRENT_VERSION = 2;

	private static final DataFixer FIXER = createFixer();

	public static DataFixer getFixer() {
		return FIXER;
	}

	private static DataFixer createFixer() {
		var builder = new DataFixerBuilder(CURRENT_VERSION);

		var v0 = builder.addSchema(0, ControlifySchemas.SchemaV0::new);
		var v1 = builder.addSchema(1, ControlifySchemas.SchemaV1::new);
		var v2 = builder.addSchema(2, ControlifySchemas.SchemaV2::new);

		builder.addFixer(new TheHolyMigrationFix(
				v1,
				GlobalSettings.defaults(),
				ProfileSettings.createDefault(CUtil.rl("generic"))
		));
		builder.addFixer(new AnalogueMovementWhitelistFix(v2));

		return builder.build().fixer();
	}

	private ControlifyDataFixer() {
	}
}
