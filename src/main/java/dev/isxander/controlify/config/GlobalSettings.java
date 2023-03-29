package dev.isxander.controlify.config;

import com.google.common.collect.Lists;
import dev.isxander.controlify.reacharound.ReachAroundMode;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

import java.util.List;

public class GlobalSettings {
    public static final GlobalSettings DEFAULT = new GlobalSettings();

    public List<Class<?>> virtualMouseScreens = Lists.newArrayList(
            AbstractContainerScreen.class
    );

    public boolean keyboardMovement = false;
    public boolean outOfFocusInput = false;
    public ReachAroundMode reachAround = ReachAroundMode.OFF;
}
