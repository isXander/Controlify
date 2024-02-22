package dev.isxander.controlify.controller;

import dev.isxander.controlify.controller.impl.ControllerStateImpl;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface ControllerState extends ControllerStateView {
    ControllerState EMPTY = new ControllerStateImpl();

    void clearState();

    default void close() {}
}
