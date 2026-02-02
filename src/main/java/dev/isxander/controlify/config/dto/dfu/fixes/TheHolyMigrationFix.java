package dev.isxander.controlify.config.dto.dfu.fixes;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import dev.isxander.controlify.config.dto.dfu.ControlifyTypeReferences;
import dev.isxander.controlify.config.settings.GlobalSettings;
import dev.isxander.controlify.config.settings.profile.BluetoothDeviceSettings;
import dev.isxander.controlify.config.settings.profile.ProfileSettings;
import dev.isxander.controlify.config.settings.profile.GenericControllerSettings;
import dev.isxander.controlify.config.settings.profile.GyroSettings;
import dev.isxander.controlify.config.settings.profile.InputSettings;

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

    private Dynamic<?> rewrite(Dynamic<?> root) {
        // Convert "controllers" array to "controller" array with restructured config
        var controllersList = root.get("controllers").orElseEmptyList();
        root = root.remove("controllers");

        root = root.set(
                "controller",
                root.createList(
                        controllersList.asStream()
                                .map(this::rewriteController)
                )
        );

        root = root.set(
                "global",
                rewriteGlobalSettings(root.get("global").orElseEmptyMap())
        );

        return root;
    }

    private Dynamic<?> rewriteController(Dynamic<?> root) {
        var config = root.get("config").orElseEmptyMap();

        // Remove old config wrapper and flatten
        root = root.remove("config");

        // Migrate generic config
        config = config.renameAndFixField(
                "controlify:config/generic",
                "generic",
                this::rewriteGenericConfig
        );

        // Migrate input config
        config = config.renameAndFixField(
                "controlify:input",
                "input",
                this::rewriteInputConfig
        );

        // Migrate rumble config
        config = config.renameAndFixField(
                "controlify:rumble",
                "rumble",
                old -> old // rumble structure stays the same
        );

        // Migrate HD haptics config
        config = config.renameAndFixField(
                "controlify:hd_haptics",
                "hd_haptic",
                old -> old // hd_haptic structure stays the same
        );

        // Add gyro if missing (new field)
        if (config.get("gyro").result().isEmpty()) {
            config = config.set("gyro", rewriteGyroConfig(root.emptyMap()));
        }

        // Add bluetooth_device if missing (new field)
        if (config.get("bluetooth_device").result().isEmpty()) {
            config = config.set("bluetooth_device", rewriteBluetoothConfig(root.emptyMap()));
        }

        // Flatten the config into the root
        return config;
    }

    private Dynamic<?> rewriteGenericConfig(Dynamic<?> root) {
        GenericControllerSettings defaults = controllerDefaults.generic;

        root = root.remove("nickname");
        root = root.remove("dont_show_controller_submission");

        // Create guide nested structure
        var guide = root.emptyMap();
        guide = guide.set(
                "verbosity",
                root.createString(defaults.guide.verbosity.getSerializedName()) // default value
        );
        guide = guide.set(
                "show_ingame_guide",
                root.createBoolean(root.get("show_ingame_guide").asBoolean(defaults.guide.showIngameGuide))
        );
        guide = guide.set(
                "ingame_guide_bottom",
                root.createBoolean(root.get("ingame_guide_bottom").asBoolean(defaults.guide.ingameGuideButtom))
        );
        guide = guide.set(
                "show_screen_guides",
                root.createBoolean(root.get("show_screen_guides").asBoolean(defaults.guide.showScreenGuides))
        );
        root = root.remove("show_ingame_guide");
        root = root.remove("ingame_guide_bottom");
        root = root.remove("show_screen_guides");
        root = root.set("guide", guide);

        // Create keyboard nested structure
        var keyboard = root.emptyMap();
        keyboard = keyboard.set(
                "show_on_screen_keyboard",
                root.createBoolean(root.get("show_on_screen_keyboard").asBoolean(defaults.keyboard.showOnScreenKeyboard))
        );
        keyboard = keyboard.set(
                "hint_cursor",
                root.createBoolean(defaults.keyboard.hintCursor)
        );
        keyboard = keyboard.set(
                "hint_command_suggester",
                root.createBoolean(defaults.keyboard.hintCommandSuggester)
        );
        keyboard = keyboard.set(
                "hint_sign_line",
                root.createBoolean(defaults.keyboard.hintSignLine)
        );
        root = root.remove("show_on_screen_keyboard");
        root = root.set("keyboard", keyboard);

        return root;
    }

    private Dynamic<?> rewriteInputConfig(Dynamic<?> root) {
        InputSettings defaults = controllerDefaults.input;

        // Create sensitivity nested structure
        var sensitivity = root.emptyMap();
        sensitivity = sensitivity.set(
                "horizontal",
                root.createFloat(root.get("h_look_sensitivity").asFloat(defaults.sensitivity.hLookSensitivity))
        );
        sensitivity = sensitivity.set(
                "vertical",
                root.createFloat(root.get("v_look_sensitivity").asFloat(defaults.sensitivity.vLookSensitivity))
        );
        sensitivity = sensitivity.set(
                "vertical_invert",
                root.createBoolean(root.get("v_look_invert").asBoolean(defaults.sensitivity.vLookInvert))
        );
        sensitivity = sensitivity.set(
                "virtual_mouse",
                root.createFloat(root.get("virtual_mouse_sensitivity").asFloat(defaults.sensitivity.virtualMouseSensitivity))
        );
        sensitivity = sensitivity.set(
                "reduce_aiming",
                root.createBoolean(root.get("reduce_aiming_sensitivity").asBoolean(defaults.sensitivity.reduceAimingSensitivity))
        );
        sensitivity = sensitivity.set(
                "look_curve",
                root.createString("standard") // default value
        );
        sensitivity = sensitivity.set(
                "is_lce",
                root.createBoolean(root.get("is_l_c_e").asBoolean(defaults.sensitivity.isLCE))
        );
        root = root.remove("h_look_sensitivity");
        root = root.remove("v_look_sensitivity");
        root = root.remove("v_look_invert");
        root = root.remove("virtual_mouse_sensitivity");
        root = root.remove("reduce_aiming_sensitivity");
        root = root.remove("is_l_c_e");
        root = root.set("sensitivity", sensitivity);

        // Create radial_menu nested structure
        var radialMenu = root.emptyMap();
        radialMenu = radialMenu.set(
                "actions",
                root.get("radial_actions").orElseEmptyList()
        );
        radialMenu = radialMenu.set(
                "button_focus_timeout_ticks",
                root.createInt(root.get("radial_button_focus_timeout_ticks").asInt(defaults.radialMenu.radialButtonFocusTimeoutTicks))
        );
        root = root.remove("radial_actions");
        root = root.remove("radial_button_focus_timeout_ticks");
        root = root.set("radial_menu", radialMenu);

        // Rename bindings to nested structure
        var bindings = root.emptyMap();
        bindings = bindings.set("bindings", root.get("bindings").orElseEmptyMap());
        root = root.remove("bindings");
        root = root.set("bindings", bindings);

        return root;
    }

    private Dynamic<?> rewriteGyroConfig(Dynamic<?> root) {
        GyroSettings defaults = controllerDefaults.gyro;

        // Create calibration nested structure
        var calibration = root.emptyMap();
        calibration = calibration.set(
                "calibration",
                root.emptyMap()
                        .set("pitch", root.createFloat(0.0f))
                        .set("yaw", root.createFloat(0.0f))
                        .set("roll", root.createFloat(0.0f))
        );
        root = root.set("calibration", calibration);

        root = root.set("look_sensitivity", root.createFloat(defaults.lookSensitivity));
        root = root.set("relative_mode", root.createBoolean(defaults.relativeMode));
        root = root.set("invert_pitch", root.createBoolean(defaults.invertPitch));
        root = root.set("invert_yaw", root.createBoolean(defaults.invertYaw));
        root = root.set("button_mode", root.createString("on")); // default value
        root = root.set("yaw_mode", root.createString("yaw")); // default value
        root = root.set("flick_stick", root.createBoolean(defaults.flickStick));

        return root;
    }

    private Dynamic<?> rewriteBluetoothConfig(Dynamic<?> root) {
        BluetoothDeviceSettings defaults = controllerDefaults.bluetoothDevice;

        root = root.set("dont_show_warning", root.createBoolean(defaults.dontShowWarning));

        return root;
    }

    private Dynamic<?> rewriteGlobalSettings(Dynamic<?> root) {
        root = root.renameField("keyboardMovement", "keyboard_movement");

        // previously the raw enum value was stored, now we store lowercase string
        root = root.renameAndFixField(
                "reach_around",
                "reach_around",
                old -> old.createString(old.asString("OFF").toLowerCase())
        );

        root = root.renameField("ui_sounds", "extra_ui_sounds");

        root = root.set("show_splitscreen_ad", root.createBoolean(globalDefaults.showSplitscreenAd));

        return root;
    }
}
