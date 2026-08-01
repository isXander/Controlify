package dev.isxander.controlify.config.dto.dfu.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;

public class GuideGuiScaleFix extends DataFix {
	private final ProfileSettings profileDefaults;

	public GuideGuiScaleFix(Schema outputSchema, ProfileSettings profileDefaults) {
		super(outputSchema, true);
		this.profileDefaults = profileDefaults;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		var profileType = getInputSchema().getType(ControlifyTypeReferences.PROFILE_CONFIG);

		return fixTypeEverywhereTyped(
			"Controlify: add guide gui scale defaults",
			profileType,
			typed -> typed.update(DSL.remainderFinder(), this::rewriteProfile)
		);
	}

	private <T> Dynamic<T> rewriteProfile(Dynamic<T> root) {
		Dynamic<T> generic = root.get("generic").orElseEmptyMap();
		Dynamic<T> guide = generic.get("guide").orElseEmptyMap();

		guide = guide
			.set("ingame_gui_scale", root.createInt(profileDefaults.generic.guide.ingameGuiScale))
			.set("screen_gui_scale", root.createInt(profileDefaults.generic.guide.screenGuiScale));

		generic = generic.set("guide", guide);
		return root.set("generic", generic);
	}
}
