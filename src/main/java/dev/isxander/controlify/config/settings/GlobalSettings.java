package dev.isxander.controlify.config.settings;

import dev.isxander.controlify.config.dto.GlobalConfig;
import dev.isxander.controlify.reacharound.ReachAroundMode;
import dev.isxander.controlify.server.ServerPolicies;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GlobalSettings {
    public Set<Class<?>> virtualMouseScreens = Set.of(
            AbstractContainerScreen.class
    );
    public boolean outOfFocusInput = false;
    public ReachAroundMode reachAround = ReachAroundMode.OFF;
    public boolean allowServerRumble = true;
    public boolean extraUiSounds = true;
    public boolean notifyLowBattery = true;
    public float ingameButtonGuideScale = 1f;
    public boolean useEnhancedSteamDeckDriver = true;
    public boolean alwaysAllowKeyboardMovement = false;
    public Set<String> keyboardMovementWhitelist;
    public Set<String> seenServers;

    private GlobalSettings() {
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
            boolean alwaysAllowKeyboardMovement,
            Set<String> keyboardMovementWhitelist,
            Set<String> seenServers
    ) {
        this.virtualMouseScreens = new HashSet<>(virtualMouseScreens);
        this.outOfFocusInput = outOfFocusInput;
        this.reachAround = reachAround;
        this.allowServerRumble = allowServerRumble;
        this.extraUiSounds = extraUiSounds;
        this.notifyLowBattery = notifyLowBattery;
        this.ingameButtonGuideScale = ingameButtonGuideScale;
        this.useEnhancedSteamDeckDriver = useEnhancedSteamDeckDriver;
        this.alwaysAllowKeyboardMovement = alwaysAllowKeyboardMovement;
        this.keyboardMovementWhitelist = new HashSet<>(keyboardMovementWhitelist);
        this.seenServers = new HashSet<>(seenServers);
    }

    public boolean shouldUseKeyboardMovement() {
        ServerData server = Minecraft.getInstance().getCurrentServer();
        return alwaysAllowKeyboardMovement
               || (server != null && keyboardMovementWhitelist.stream().anyMatch(server.ip::endsWith))
               || ServerPolicies.KEYBOARD_LIKE_MOVEMENT.get();
    }

    public static GlobalSettings defaults() {
        return new GlobalSettings();
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
                dto.outOfFocusInput(),
                dto.reachAround(),
                dto.allowServerRumble(),
                dto.extraUiSounds(),
                dto.notifyLowBattery(),
                dto.ingameButtonGuideScale(),
                dto.useEnhancedSteamDeckDriver(),
                dto.alwaysAllowKeyboardMovement(),
                Set.copyOf(dto.keyboardMovementWhitelist()),
                Set.copyOf(dto.seenServers())
        );
    }

    public GlobalConfig toDTO() {
        return new GlobalConfig(
                virtualMouseScreens
                        .stream()
                        .map(Class::getName)
                        .toList(),
                outOfFocusInput,
                reachAround,
                allowServerRumble,
                extraUiSounds,
                notifyLowBattery,
                ingameButtonGuideScale,
                useEnhancedSteamDeckDriver,
                alwaysAllowKeyboardMovement,
                List.copyOf(keyboardMovementWhitelist),
                List.copyOf(seenServers)
        );
    }
}
