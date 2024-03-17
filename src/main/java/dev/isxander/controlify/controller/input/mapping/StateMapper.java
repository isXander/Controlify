package dev.isxander.controlify.controller.input.mapping;

import dev.isxander.controlify.controller.input.ControllerState;

public interface StateMapper {
   ControllerState mapState(ControllerState state);
}
