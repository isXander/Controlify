package dev.isxander.controlify.controller.composable.emulatedgp.mapping;

import dev.isxander.controlify.controller.composable.ComposableControllerState;
import dev.isxander.controlify.controller.composable.ModifiableControllerState;
import dev.isxander.controlify.controller.composable.impl.ComposableControllerStateImpl;

import java.util.ArrayList;
import java.util.List;

public record UserGamepadMapping(
        String inputDriverName,

        List<MappingEntry.FromButton> buttonMappings,
        List<MappingEntry.FromAxis> axisMappings,
        List<MappingEntry.FromHat> hatMappings
) implements GamepadMapping {
    @Override
    public ComposableControllerState mapJoystick(ComposableControllerState state) {
        ModifiableControllerState newState = new ComposableControllerStateImpl();

        for (MappingEntry.FromButton mapping : buttonMappings) {
            mapping.apply(state, newState);
        }
        for (MappingEntry.FromAxis mapping : axisMappings) {
            mapping.apply(state, newState);
        }
        for (MappingEntry.FromHat mapping : hatMappings) {
            mapping.apply(state, newState);
        }

        return newState;
    }

    public boolean isNoMapping() {
        return this.inputDriverName().equals(NO_MAPPING.inputDriverName());
    }

    public static final UserGamepadMapping NO_MAPPING = new Builder().build();

    public static class Builder {
        private String inputDriverName = "None";

        private final List<MappingEntry.FromButton> buttonMappings = new ArrayList<>();
        private final List<MappingEntry.FromAxis> axisMappings = new ArrayList<>();
        private final List<MappingEntry.FromHat> hatMappings = new ArrayList<>();

        public Builder putButtonMapping(MappingEntry.FromButton mapping) {
            buttonMappings.add(mapping);
            return this;
        }

        public Builder putAxisMapping(MappingEntry.FromAxis mapping) {
            axisMappings.add(mapping);
            return this;
        }

        public Builder putHatMapping(MappingEntry.FromHat mapping) {
            hatMappings.add(mapping);
            return this;
        }

        public Builder inputDriverName(String inputDriverName) {
            this.inputDriverName = inputDriverName;
            return this;
        }

        public UserGamepadMapping build() {
            return new UserGamepadMapping(
                    inputDriverName,

                    buttonMappings,
                    axisMappings,
                    hatMappings
            );
        }
    }
}
