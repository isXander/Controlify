package dev.isxander.controlify.gametest.mixin;

import dev.isxander.controlify.config.settings.ControlifySettings;
import dev.isxander.controlify.config.settings.GlobalSettings;
import dev.isxander.controlify.config.settings.device.DeviceSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import it.unimi.dsi.fastutil.ints.Int2ObjectSortedMap;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ControlifySettings.class)
public interface ControlifySettingsAccessor {
	@Accessor("globalSettings")
	void controlify_test$setGlobalSettings(GlobalSettings globalSettings);

	@Accessor("profileSettings")
	Int2ObjectSortedMap<ProfileSettings> controlify_test$getProfileSettings();

	@Accessor("deviceSettings")
	Map<String, DeviceSettings> controlify_test$getDeviceSettings();
}
