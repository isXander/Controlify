package dev.isxander.controlify.config.settings;

import com.google.common.collect.Sets;
import dev.isxander.controlify.config.dto.GlobalConfig;
import dev.isxander.controlify.reacharound.ReachAroundMode;
import dev.isxander.controlify.server.ServerPolicies;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GlobalSettings {
    public final Set<Class<?>> virtualMouseScreens;
    public boolean mixedInput;
    public boolean outOfFocusInput;
    public ReachAroundMode reachAround;
    public boolean allowServerRumble;
    public boolean extraUiSounds;
    public boolean notifyLowBattery;
    public float ingameButtonGuideScale;
    public boolean useEnhancedSteamDeckDriver;
    public boolean alwaysKeyboardMovement;
    public List<String> keyboardMovementWhitelist;
    public final Set<String> seenServers;
    public boolean showSplitscreenAd;
    public boolean autoSwitchControllers;

    private static final GlobalSettings DEFAULT = new GlobalSettings();

    private GlobalSettings() {
        this.virtualMouseScreens = Sets.newHashSet(
                AbstractContainerScreen.class
        );
        this.mixedInput = false;
        this.outOfFocusInput = false;
        this.reachAround = ReachAroundMode.OFF;
        this.allowServerRumble = true;
        this.extraUiSounds = true;
        this.notifyLowBattery = true;
        this.ingameButtonGuideScale = 1f;
        this.useEnhancedSteamDeckDriver = true;
        this.alwaysKeyboardMovement = false;
        this.keyboardMovementWhitelist = new ArrayList<>();
        this.seenServers = new HashSet<>();
        this.showSplitscreenAd = true;
        this.autoSwitchControllers = true;
    }

    public GlobalSettings(
            Set<Class<?>> virtualMouseScreens,
            boolean mixedInput,
            boolean outOfFocusInput,
            ReachAroundMode reachAround,
            boolean allowServerRumble,
            boolean extraUiSounds,
            boolean notifyLowBattery,
            float ingameButtonGuideScale,
            boolean useEnhancedSteamDeckDriver,
            boolean alwaysKeyboardMovement,
            List<String> keyboardMovementWhitelist,
            Set<String> seenServers,
            boolean showSplitscreenAd,
            boolean autoSwitchControllers
    ) {
        this.virtualMouseScreens = new HashSet<>(virtualMouseScreens);
        this.mixedInput = mixedInput;
        this.outOfFocusInput = outOfFocusInput;
        this.reachAround = reachAround;
        this.allowServerRumble = allowServerRumble;
        this.extraUiSounds = extraUiSounds;
        this.notifyLowBattery = notifyLowBattery;
        this.ingameButtonGuideScale = ingameButtonGuideScale;
        this.useEnhancedSteamDeckDriver = useEnhancedSteamDeckDriver;
        this.alwaysKeyboardMovement = alwaysKeyboardMovement;
        this.keyboardMovementWhitelist = new ArrayList<>(keyboardMovementWhitelist);
        this.seenServers = new HashSet<>(seenServers);
        this.showSplitscreenAd = showSplitscreenAd;
        this.autoSwitchControllers = autoSwitchControllers;
    }

    public boolean shouldUseKeyboardMovement() {
        ServerData server = Minecraft.getInstance().getCurrentServer();
        return alwaysKeyboardMovement
               || (server != null && keyboardMovementWhitelist.stream().anyMatch(server.ip::endsWith))
               || ServerPolicies.KEYBOARD_LIKE_MOVEMENT.get();
    }

    public static GlobalSettings defaults() {
        return DEFAULT;
    }

    public static GlobalSettings fromDTO(GlobalConfig dto) {
        return new GlobalSettings(
                dto.virtualMouseScreens()
                        .stream()
                        .flatMap(className -> {
                            try {
                                return Stream.of(Class.forName(className));
                            } catch (ClassNotFoundException e) {
                                return Stream.empty();
                            }
                        })
                        .collect(Collectors.toSet()),
                dto.mixedInput(),
                dto.outOfFocusInput(),
                dto.reachAround(),
                dto.allowServerRumble(),
                dto.extraUiSounds(),
                dto.notifyLowBattery(),
                dto.ingameButtonGuideScale(),
                dto.useEnhancedSteamDeckDriver(),
                dto.alwaysAllowKeyboardMovement(),
                List.copyOf(dto.keyboardMovementWhitelist()),
                Set.copyOf(dto.seenServers()),
                dto.showSplitscreenAd(),
                dto.autoSwitchControllers()
        );
    }

    public GlobalConfig toDTO() {
        return new GlobalConfig(
                virtualMouseScreens
                        .stream()
                        .map(Class::getName)
                        .toList(),
                mixedInput,
                outOfFocusInput,
                reachAround,
                allowServerRumble,
                extraUiSounds,
                notifyLowBattery,
                ingameButtonGuideScale,
                useEnhancedSteamDeckDriver,
                alwaysKeyboardMovement,
                List.copyOf(keyboardMovementWhitelist),
                List.copyOf(seenServers),
                showSplitscreenAd,
                autoSwitchControllers
        );
    }
}
