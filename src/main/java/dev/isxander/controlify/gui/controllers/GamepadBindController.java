package dev.isxander.controlify.gui.controllers;

import dev.isxander.controlify.bindings.GamepadBind;
import dev.isxander.controlify.bindings.GamepadBinds;
import dev.isxander.controlify.bindings.IBind;
import dev.isxander.controlify.controller.gamepad.GamepadController;
import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;

import java.util.Optional;

public class GamepadBindController extends AbstractBindController<GamepadState> {
    public GamepadBindController(Option<IBind<GamepadState>> option, GamepadController controller) {
        super(option, controller);
    }

    @Override
    public AbstractBindControllerElement<GamepadState> provideWidget(YACLScreen yaclScreen, Dimension<Integer> dimension) {
        return new BindButtonWidget(this, yaclScreen, dimension);
    }

    public static class BindButtonWidget extends AbstractBindControllerElement<GamepadState> {
        public BindButtonWidget(GamepadBindController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        public Optional<IBind<GamepadState>> getPressedBind() {
            var gamepad = (GamepadController) control.controller;

            for (var bindType : GamepadBinds.values()) {
                GamepadBind bind = bindType.forGamepad(gamepad);
                if (bind.held(gamepad.state()) && !bind.held(gamepad.prevState())) {
                    return Optional.of(bind);
                }
            }

            return Optional.empty();
        }
    }
}
