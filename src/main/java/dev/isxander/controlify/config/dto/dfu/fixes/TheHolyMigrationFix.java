package dev.isxander.controlify.config.dto.dfu.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;
import dev.isxander.controlify.config.settings.GlobalSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

public final class TheHolyMigrationFix extends DataFix {
    private final GlobalSettings globalDefaults;
    private final ProfileSettings controllerDefaults;

    public TheHolyMigrationFix(Schema outputSchema, GlobalSettings globalDefaults, ProfileSettings controllerDefaults) {
        super(outputSchema, true);
        this.globalDefaults = globalDefaults;
        this.controllerDefaults = controllerDefaults;
    }

    @Override
    protected TypeRewriteRule makeRule() {
        var type = getInputSchema().getType(ControlifyTypeReferences.USER_STATE);

        return fixTypeEverywhereTyped(
                "Controlify: controllers map to single controller",
                type,
                typed -> typed.update(
                        DSL.remainderFinder(),
                        this::rewrite
                )
        );
    }

    private <T> Dynamic<T> rewrite(Dynamic<T> root) {
        // Get current controller UID to know which controller config to migrate
        Optional<String> currentControllerUid = root.get("current_controller").asString().result();
        Dynamic<T> controllersMap = root.get("controllers").orElseEmptyMap();

        // Create the profiles array with the current controller as profile 0
        Dynamic<T> profiles = root.createList(Stream.empty());
        Dynamic<T> devices = root.createMap(Map.of());
        Optional<Dynamic<T>> mixedInputFromController = Optional.empty();

        if (currentControllerUid.isPresent()) {
            String uid = currentControllerUid.get();
            Dynamic<T> controllerConfig = controllersMap.get(uid).orElseEmptyMap().get("config").orElseEmptyMap();

            // Create the profile from the old controller config
            Dynamic<T> profile = rewriteControllerConfigToProfile(controllerConfig, root);
            profiles = root.createList(Stream.of(profile));

            // Extract mixed_input setting from input config (moved to global in new format)
            Dynamic<T> inputConfig = controllerConfig.get("controlify:input").orElseEmptyMap();
            mixedInputFromController = inputConfig.get("mixed_input").result();

            // Extract gyro calibration for the devices map
            Dynamic<T> gyroConfig = controllerConfig.get("controlify:gyro").orElseEmptyMap();
            Optional<Dynamic<T>> calibration = gyroConfig.get("calibration").result();

            if (calibration.isPresent()) {
                Dynamic<T> cal = calibration.get();
                Dynamic<T> gyroCalibration = root.createMap(Map.of(
                        root.createString("offset"), root.createMap(Map.of(
                                root.createString("pitch"), cal.get("x").orElseEmptyMap(),
                                root.createString("yaw"), cal.get("y").orElseEmptyMap(),
                                root.createString("roll"), cal.get("z").orElseEmptyMap()
                        ))
                ));
                Dynamic<T> deviceConfig = root.createMap(Map.of(
                        root.createString("gyro_calibration"), gyroCalibration
                ));
                devices = root.createMap(Map.of(
                        root.createString(uid), deviceConfig
                ));
            }
        }

        root = root.set("profiles", profiles);
        root = root.set("devices", devices);

        // Remove the old fields
        root = root.remove("current_controller");
        root = root.remove("controllers");

        // Rewrite global settings
        root = root.set(
                "global",
                rewriteGlobalSettings(root.get("global").orElseEmptyMap(), mixedInputFromController)
        );

        return root;
    }

    private <T> Dynamic<T> rewriteControllerConfigToProfile(Dynamic<T> controllerConfig, Dynamic<T> root) {
        Dynamic<T> inputConfig = controllerConfig.get("controlify:input").orElseEmptyMap();
        Dynamic<T> genericConfig = controllerConfig.get("controlify:config/generic").orElseEmptyMap();
        Dynamic<T> rumbleConfig = controllerConfig.get("controlify:rumble").orElseEmptyMap();
        Dynamic<T> hdHapticsConfig = controllerConfig.get("controlify:hd_haptics").orElseEmptyMap();
        Dynamic<T> gyroConfig = controllerConfig.get("controlify:gyro").orElseEmptyMap();

        // Build the new profile structure
        return root.createMap(Map.of(
                root.createString("generic"), rewriteGenericConfig(genericConfig, root),
                root.createString("input"), rewriteInputConfig(inputConfig, root),
                root.createString("rumble"), rewriteRumbleConfig(rumbleConfig, root),
                root.createString("hd_haptic"), rewriteHdHapticConfig(hdHapticsConfig, root),
                root.createString("gyro"), rewriteGyroConfig(gyroConfig, root),
                root.createString("bluetooth_device"), rewriteBluetoothDeviceConfig(root)
        ));
    }

    private <T> Dynamic<T> rewriteGenericConfig(Dynamic<T> old, Dynamic<T> root) {
        var defaults = controllerDefaults.generic;

        // Build guide sub-object
        Dynamic<T> guide = root.createMap(Map.of(
                root.createString("verbosity"), root.createString(
                        old.get("guide_verbosity").asString().result().orElse("FULL").toLowerCase()
                ),
                root.createString("show_ingame_guide"), old.get("show_ingame_guide")
                        .result().orElse(root.createBoolean(defaults.guide.showIngameGuide)),
                root.createString("ingame_guide_bottom"), old.get("ingame_guide_bottom")
                        .result().orElse(root.createBoolean(defaults.guide.ingameGuideBottom)),
                root.createString("show_screen_guides"), old.get("show_screen_guides")
                        .result().orElse(root.createBoolean(defaults.guide.showScreenGuides))
        ));

        // Build keyboard sub-object
        Dynamic<T> keyboard = root.createMap(Map.of(
                root.createString("show_on_screen_keyboard"), old.get("show_on_screen_keyboard")
                        .result().orElse(root.createBoolean(defaults.keyboard.showOnScreenKeyboard)),
                root.createString("hint_cursor"), old.get("hint_keyboard_cursor")
                        .result().orElse(root.createBoolean(defaults.keyboard.hintCursor)),
                root.createString("hint_command_suggester"), old.get("hint_keyboard_command_suggester")
                        .result().orElse(root.createBoolean(defaults.keyboard.hintCommandSuggester)),
                root.createString("hint_sign_line"), old.get("hint_keyboard_sign_line")
                        .result().orElse(root.createBoolean(defaults.keyboard.hintSignLine))
        ));

        return root.createMap(Map.of(
                root.createString("auto_jump"), old.get("auto_jump")
                        .result().orElse(root.createBoolean(defaults.autoJump)),
                root.createString("toggle_sprint"), old.get("toggle_sprint")
                        .result().orElse(root.createBoolean(defaults.toggleSprint)),
                root.createString("toggle_sneak"), old.get("toggle_sneak")
                        .result().orElse(root.createBoolean(defaults.toggleSneak)),
                root.createString("disable_fly_drifting"), old.get("disable_fly_drifting")
                        .result().orElse(root.createBoolean(defaults.disableFlyDrifting)),
                root.createString("guide"), guide,
                root.createString("keyboard"), keyboard
        ));
    }

    private <T> Dynamic<T> rewriteInputConfig(Dynamic<T> old, Dynamic<T> root) {
        var defaults = controllerDefaults.input;

        // Build sensitivity sub-object
        Dynamic<T> sensitivity = root.createMap(Map.of(
                root.createString("horizontal"), old.get("h_look_sensitivity")
                        .result().orElse(root.createFloat(defaults.sensitivity.hLookSensitivity)),
                root.createString("vertical"), old.get("v_look_sensitivity")
                        .result().orElse(root.createFloat(defaults.sensitivity.vLookSensitivity)),
                root.createString("vertical_invert"), old.get("v_look_invert")
                        .result().orElse(root.createBoolean(defaults.sensitivity.vLookInvert)),
                root.createString("virtual_mouse"), old.get("virtual_mouse_sensitivity")
                        .result().orElse(root.createFloat(defaults.sensitivity.virtualMouseSensitivity)),
                root.createString("reduce_aiming"), old.get("reduce_aiming_sensitivity")
                        .result().orElse(root.createBoolean(defaults.sensitivity.reduceAimingSensitivity)),
                root.createString("look_curve"), root.createString(
                        old.get("look_input_curve").asString().result().orElse("STANDARD").toLowerCase()
                ),
                root.createString("is_lce"), old.get("is_l_c_e")
                        .result().orElse(root.createBoolean(defaults.sensitivity.isLCE)),
                root.createString("default_deadzone"), root.createFloat(defaults.sensitivity.defaultDeadzone)
        ));

        // Build radial_menu sub-object
        Dynamic<T> radialMenu = root.createMap(Map.of(
                root.createString("actions"), old.get("radial_actions")
                        .result().orElse(root.createList(defaults.radialMenu.radialActions.stream()
                                .map(id -> root.createString(id.toString())))),
                root.createString("button_focus_timeout_ticks"), old.get("radial_button_focus_timeout_ticks")
                        .result().orElse(root.createInt(defaults.radialMenu.radialButtonFocusTimeoutTicks))
        ));

        return root.createMap(Map.of(
                root.createString("sensitivity"), sensitivity,
                root.createString("radial_menu"), radialMenu,
                root.createString("button_activation_threshold"), old.get("button_activation_threshold")
                        .result().orElse(root.createFloat(defaults.buttonActivationThreshold)),
                root.createString("keep_default_bindings"), old.get("keep_default_bindings")
                        .result().orElse(root.createBoolean(defaults.keepDefaultBindings))
        ));
    }

    private <T> Dynamic<T> rewriteRumbleConfig(Dynamic<T> old, Dynamic<T> root) {
        var defaults = controllerDefaults.rumble;

        return root.createMap(Map.of(
                root.createString("enabled"), old.get("enabled")
                        .result().orElse(root.createBoolean(defaults.enabled))
        ));
    }

    private <T> Dynamic<T> rewriteHdHapticConfig(Dynamic<T> old, Dynamic<T> root) {
        var defaults = controllerDefaults.hdHaptic;

        return root.createMap(Map.of(
                root.createString("enabled"), old.get("enabled")
                        .result().orElse(root.createBoolean(defaults.enabled))
        ));
    }

    private <T> Dynamic<T> rewriteGyroConfig(Dynamic<T> old, Dynamic<T> root) {
        var defaults = controllerDefaults.gyro;

        return root.createMap(Map.of(
                root.createString("look_sensitivity"), old.get("look_sensitivity")
                        .result().orElse(root.createFloat(defaults.lookSensitivity)),
                root.createString("relative_mode"), old.get("relative_gyro_mode")
                        .result().orElse(root.createBoolean(defaults.relativeMode)),
                root.createString("invert_pitch"), old.get("invert_y")
                        .result().orElse(root.createBoolean(defaults.invertPitch)),
                root.createString("invert_yaw"), old.get("invert_x")
                        .result().orElse(root.createBoolean(defaults.invertYaw)),
                root.createString("button_mode"), root.createString(
                        old.get("requires_button").asString().result().orElse("ON").toLowerCase()
                ),
                root.createString("yaw_mode"), root.createString(
                        old.get("yaw_mode").asString().result().orElse("YAW").toLowerCase()
                ),
                root.createString("flick_stick"), old.get("flick_stick")
                        .result().orElse(root.createBoolean(defaults.flickStick))
        ));
    }

    private <T> Dynamic<T> rewriteBluetoothDeviceConfig(Dynamic<T> root) {
        var defaults = controllerDefaults.bluetoothDevice;

        return root.createMap(Map.of(
                root.createString("dont_show_warning"), root.createBoolean(defaults.dontShowWarning)
        ));
    }

    private <T> Dynamic<T> rewriteGlobalSettings(Dynamic<T> root, Optional<Dynamic<T>> mixedInputFromController) {
        root = root.renameField("keyboardMovement", "keyboard_movement");

        // previously the raw enum value was stored, now we store lowercase string
        root = root.renameAndFixField(
                "reach_around",
                "reach_around",
                old -> old.createString(old.asString().result().orElse("OFF").toLowerCase())
        );

        root = root.renameField("ui_sounds", "extra_ui_sounds");

        root = root.set("show_splitscreen_ad", root.createBoolean(globalDefaults.showSplitscreenAd));

        // mixed_input was previously in the controller config, now it's in global
        // Prefer value from old controller config, fall back to global if present, else default
        if (root.get("mixed_input").result().isEmpty()) {
            root = root.set("mixed_input", mixedInputFromController
                    .orElse(root.createBoolean(globalDefaults.mixedInput)));
        }

        return root;
    }
}
