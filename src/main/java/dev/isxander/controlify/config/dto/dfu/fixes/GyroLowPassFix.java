package dev.isxander.controlify.config.dto.dfu.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;

public class GyroLowPassFix extends DataFix {
	private final ProfileSettings profileDefaults;

	public GyroLowPassFix(Schema outputSchema, ProfileSettings profileDefaults) {
		super(outputSchema, true);
		this.profileDefaults = profileDefaults;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var profileType = getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG);

		return fixTypeEverywhereTyped(
				"Controlify: add gyro low pass hz setting",
				profileType,
				typed -> typed.update(DSL.remainderFinder(), this::rewriteProfile)
		);
	}

	private <T> Dynamic<T> rewriteProfile(Dynamic<T> root) {
		var gyro = root.get("gyro").orElseEmptyMap();

		gyro = gyro.set("low_pass_cutoff_hz", root.createFloat(profileDefaults.gyro.lowPassCutoffHz));

		return root.set("gyro", gyro);
	}
}
