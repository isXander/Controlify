package dev.isxander.controlify.config.dto;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.isxander.controlify.reacharound.ReachAroundMode;

import java.util.List;

public record GlobalConfig(
        List<String> virtualMouseScreens,
        boolean mixedInput,
        boolean outOfFocusInput,
        ReachAroundMode reachAround,
        boolean allowServerRumble,
        boolean extraUiSounds,
        boolean notifyLowBattery,
        float ingameButtonGuideScale,
        boolean useEnhancedSteamDeckDriver,
        boolean alwaysAllowKeyboardMovement,
        List<String> keyboardMovementWhitelist,
        List<String> seenServers,
        boolean showSplitscreenAd,
        boolean autoSwitchControllers,
        String preferredControllerUid
) {
    public static final Codec<GlobalConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Codec.STRING).fieldOf("virtual_mouse_screens").forGetter(GlobalConfig::virtualMouseScreens),
            Codec.BOOL.fieldOf("mixed_input").forGetter(GlobalConfig::mixedInput),
            Codec.BOOL.fieldOf("out_of_focus_input").forGetter(GlobalConfig::outOfFocusInput),
            ReachAroundMode.CODEC.fieldOf("reach_around").forGetter(GlobalConfig::reachAround),
            Codec.BOOL.fieldOf("allow_server_rumble").forGetter(GlobalConfig::allowServerRumble),
            Codec.BOOL.fieldOf("extra_ui_sounds").forGetter(GlobalConfig::extraUiSounds),
            Codec.BOOL.fieldOf("notify_low_battery").forGetter(GlobalConfig::notifyLowBattery),
            Codec.FLOAT.fieldOf("ingame_button_guide_scale").forGetter(GlobalConfig::ingameButtonGuideScale),
            Codec.BOOL.fieldOf("use_enhanced_steam_deck_driver").forGetter(GlobalConfig::useEnhancedSteamDeckDriver),
            Codec.BOOL.fieldOf("keyboard_movement").forGetter(GlobalConfig::alwaysAllowKeyboardMovement),
            Codec.list(Codec.STRING).fieldOf("keyboard_movement_whitelist").forGetter(GlobalConfig::keyboardMovementWhitelist),
            Codec.list(Codec.STRING).fieldOf("seen_servers").forGetter(GlobalConfig::seenServers),
            Codec.BOOL.fieldOf("show_splitscreen_ad").forGetter(GlobalConfig::showSplitscreenAd),
            Codec.BOOL.optionalFieldOf("auto_switch_controllers", true).forGetter(GlobalConfig::autoSwitchControllers),
            Codec.STRING.optionalFieldOf("preferred_controller_uid", "").forGetter(GlobalConfig::preferredControllerUid)
    ).apply(instance, GlobalConfig::new));
}
