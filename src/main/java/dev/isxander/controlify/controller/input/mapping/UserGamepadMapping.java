package dev.isxander.controlify.controller.input.mapping;

import dev.isxander.controlify.controller.input.ControllerState;
import dev.isxander.controlify.controller.input.ModifiableControllerState;
import dev.isxander.controlify.controller.impl.ControllerStateImpl;

import java.util.ArrayList;
import java.util.List;

public record UserGamepadMapping(
        List<MappingEntry> mappings
) implements GamepadMapping {
    @Override
    public ControllerState mapJoystick(ControllerState state) {
        ModifiableControllerState newState = new ControllerStateImpl();

        for (MappingEntry mapping : mappings) {
            mapping.apply(state, newState);
        }

        return newState;
    }

    public static final UserGamepadMapping NO_MAPPING = new Builder().build();

    public static class Builder {
        private final List<MappingEntry> mappings = new ArrayList<>();

        public Builder putMapping(MappingEntry mapping) {
            if (mapping == null)
                return this;

            mappings.add(mapping);
            return this;
        }

        public UserGamepadMapping build() {
            return new UserGamepadMapping(mappings);
        }
    }
}
