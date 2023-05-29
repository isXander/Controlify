package dev.isxander.controlify.controller.joystick;

import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.controller.Controller;
import dev.isxander.controlify.controller.joystick.JoystickConfig;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.controlify.controller.joystick.mapping.JoystickMapping;
import dev.isxander.controlify.controller.joystick.mapping.UnmappedJoystickMapping;
import net.minecraft.resources.ResourceLocation;

public interface JoystickController<T extends JoystickConfig> extends Controller<JoystickState, T> {
    JoystickMapping mapping();

    @Override
    default ResourceLocation icon() {
        return Controlify.id("textures/gui/joystick/icon.png");
    }

    @Deprecated
    int axisCount();
    @Deprecated
    int buttonCount();
    @Deprecated
    int hatCount();

    @Override
    default boolean canBeUsed() {
        return !(mapping() instanceof UnmappedJoystickMapping);
    }
}
