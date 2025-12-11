package dev.isxander.controlify.controller.input;

import net.minecraft.resources.Identifier;

import java.util.List;

public record DeadzoneGroup(
        Identifier name,
        List<Identifier> axes
) {
}
