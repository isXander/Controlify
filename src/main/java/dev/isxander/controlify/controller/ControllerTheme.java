package dev.isxander.controlify.controller;

import dev.isxander.yacl.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum ControllerTheme implements NameableEnum {
    XBOX_ONE("xbox"),
    DUALSHOCK4("dualshock4"),
    DUALSHOCK3("dualshock3");

    private final String id;

    ControllerTheme(String id) {
        this.id = id;
    }

    public String id() {
        return id;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("controlify.controller_theme." + name().toLowerCase());
    }
}
