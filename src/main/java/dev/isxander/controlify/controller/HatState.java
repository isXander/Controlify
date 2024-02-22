package dev.isxander.controlify.controller;

import dev.isxander.yacl3.api.NameableEnum;
import net.minecraft.network.chat.Component;

public enum HatState implements NameableEnum {
    CENTERED,
    UP,
    RIGHT,
    DOWN,
    LEFT,
    RIGHT_UP,
    RIGHT_DOWN,
    LEFT_UP,
    LEFT_DOWN;

    public boolean isCentered() {
        return this == CENTERED;
    }

    public boolean isRight() {
        return this == RIGHT || this == RIGHT_UP || this == RIGHT_DOWN;
    }

    public boolean isUp() {
        return this == UP || this == RIGHT_UP || this == LEFT_UP;
    }

    public boolean isLeft() {
        return this == LEFT || this == LEFT_UP || this == LEFT_DOWN;
    }

    public boolean isDown() {
        return this == DOWN || this == RIGHT_DOWN || this == LEFT_DOWN;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("controlify.hat_state." + this.name().toLowerCase());
    }
}
