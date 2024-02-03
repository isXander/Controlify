package dev.isxander.controlify.controller.composable.impl;

import dev.isxander.controlify.controller.ControllerConfig;
import dev.isxander.controlify.controller.composable.ControllerStateModifier;
import dev.isxander.controlify.controller.composable.ModifiableControllerState;
import dev.isxander.controlify.utils.ControllerUtils;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public class DeadzoneControllerStateModifier implements ControllerStateModifier {
    @Override
    public void modifyState(ModifiableControllerState stateProvider, ControllerConfig config) {
        for (ResourceLocation axis : stateProvider.getAxes()) {
            float before = stateProvider.getAxisState(axis);
            float deadzone = config.deadzones.getOrDefault(axis, 0f);
            if (deadzone == 0f)
                continue;

            float after = ControllerUtils.deadzone(before, deadzone);

            stateProvider.setAxis(axis, after);
        }
    }
}
