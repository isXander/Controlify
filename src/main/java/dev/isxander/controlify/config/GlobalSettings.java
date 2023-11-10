package dev.isxander.controlify.config;

import com.google.common.collect.Lists;
import com.google.gson.annotations.SerializedName;
import dev.isxander.controlify.reacharound.ReachAroundMode;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ServerData;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GlobalSettings {
    public static final GlobalSettings DEFAULT = new GlobalSettings();

    public List<Class<?>> virtualMouseScreens = Lists.newArrayList(
            AbstractContainerScreen.class
    );

    @SerializedName("keyboardMovement") public boolean alwaysKeyboardMovement = false;
    public List<String> keyboardMovementWhitelist = new ArrayList<>();
    public boolean outOfFocusInput = false;
    public boolean loadVibrationNatives = false;
    public String customVibrationNativesPath = "";
    public boolean vibrationOnboarded = false;
    public ReachAroundMode reachAround = ReachAroundMode.OFF;
    public boolean allowServerRumble = true;
    public boolean uiSounds = false;
    public boolean notifyLowBattery = true;
    public boolean quietMode = false;
    public float ingameButtonGuideScale = 1f;

    public Set<String> seenServers = new HashSet<>();

    public boolean shouldUseKeyboardMovement() {
        ServerData server = Minecraft.getInstance().getCurrentServer();
        return alwaysKeyboardMovement
                || (server != null && keyboardMovementWhitelist.stream().anyMatch(server.ip::endsWith));
    }
}
