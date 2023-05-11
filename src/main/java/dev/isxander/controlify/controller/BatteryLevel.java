package dev.isxander.controlify.controller;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public enum BatteryLevel {
    EMPTY, LOW, MEDIUM, FULL, MAX,
    WIRED, UNKNOWN;

    public MutableComponent getFriendlyName() {
        return Component.translatable("controlify.battery_level." + name().toLowerCase()).withStyle(
                switch (this) {
                    case EMPTY, LOW -> ChatFormatting.RED;
                    case MEDIUM -> ChatFormatting.YELLOW;
                    case FULL, MAX -> ChatFormatting.GREEN;
                    default -> ChatFormatting.WHITE;
                }
        );
    }
}
