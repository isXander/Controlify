package dev.isxander.controlify.config3.settings;

import dev.isxander.controlify.config3.dto.GlobalConfig;
import dev.isxander.controlify.reacharound.ReachAroundMode;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalSettings {
    public Set<Class<?>> virtualMouseScreens = Set.of(
            AbstractContainerScreen.class
    );
    public boolean outOfFocusInput;
    public ReachAroundMode reachAround;
    public boolean allowServerRumble;
    public boolean extraUiSounds;
    public boolean notifyLowBattery;
    public float ingameButtonGuideScale;
    public boolean useEnhancedSteamDeckDriver;
    public Set<String> keyboardMovementWhitelist;
    public Set<String> seenServers;

    public GlobalSettings() {
    }

    public GlobalSettings(
            Set<Class<?>> virtualMouseScreens,
            boolean outOfFocusInput,
            ReachAroundMode reachAround,
            boolean allowServerRumble,
            boolean extraUiSounds,
            boolean notifyLowBattery,
            float ingameButtonGuideScale,
            boolean useEnhancedSteamDeckDriver,
            Set<String> keyboardMovementWhitelist,
            Set<String> seenServers
    ) {
        this.virtualMouseScreens = new ArrayList<>(virtualMouseScreens);
        this.outOfFocusInput = outOfFocusInput;
        this.reachAround = reachAround;
        this.allowServerRumble = allowServerRumble;
        this.extraUiSounds = extraUiSounds;
        this.notifyLowBattery = notifyLowBattery;
        this.ingameButtonGuideScale = ingameButtonGuideScale;
        this.useEnhancedSteamDeckDriver = useEnhancedSteamDeckDriver;
        this.keyboardMovementWhitelist = new HashSet<>(keyboardMovementWhitelist);
        this.seenServers = new HashSet<>(seenServers);
    }

    public static GlobalSettings fromDTO(GlobalConfig dto) {
        return new GlobalSettings(
                dto.virtualMouseScreens(),
                dto.outOfFocusInput(),
                dto.reachAround(),
                dto.allowServerRumble(),
                dto.extraUiSounds(),
                dto.notifyLowBattery(),
                dto.ingameButtonGuideScale(),
                dto.useEnhancedSteamDeckDriver(),
                dto.keyboardMovementWhitelist(),
                dto.seenServers()
        );
    }

    public GlobalConfig toDTO() {
        return new GlobalConfig(
                List.copyOf(virtualMouseScreens),
                outOfFocusInput,
                reachAround,
                allowServerRumble,
                extraUiSounds,
                notifyLowBattery,
                ingameButtonGuideScale,
                useEnhancedSteamDeckDriver,
                List.copyOf(keyboardMovementWhitelist),
                List.copyOf(seenServers)
        );
    }
}
