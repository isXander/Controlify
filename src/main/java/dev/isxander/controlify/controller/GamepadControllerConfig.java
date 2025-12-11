package dev.isxander.controlify.controller;

import dev.isxander.controlify.controller.serialization.ConfigClass;
import dev.isxander.controlify.utils.CUtil;
import net.minecraft.resources.Identifier;

public class GamepadControllerConfig implements ConfigClass {
    public static final Identifier ID = CUtil.rl("config/gamepad");
}
