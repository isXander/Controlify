package dev.isxander.controlify.gui.controllers;

import dev.isxander.controlify.bindings.*;
import dev.isxander.controlify.controller.joystick.JoystickController;
import dev.isxander.controlify.controller.joystick.JoystickState;
import dev.isxander.yacl3.api.Option;
import dev.isxander.yacl3.api.utils.Dimension;
import dev.isxander.yacl3.gui.YACLScreen;

import java.util.Optional;

public class JoystickBindController extends AbstractBindController<JoystickState> {
    public JoystickBindController(Option<IBind<JoystickState>> option, JoystickController<?> controller) {
        super(option, controller);
    }
    @Override
    public AbstractBindControllerElement<JoystickState> provideWidget(YACLScreen yaclScreen, Dimension<Integer> dimension) {
        return new BindButtonWidget(this, yaclScreen, dimension);
    }

    public static class BindButtonWidget extends AbstractBindControllerElement<JoystickState> {
        public BindButtonWidget(JoystickBindController control, YACLScreen screen, Dimension<Integer> dim) {
            super(control, screen, dim);
        }

        @Override
        public Optional<IBind<JoystickState>> getPressedBind() {
            var joystick = (JoystickController<?>) control.controller;

            var state = joystick.state();
            var prevState = joystick.prevState();

            for (int i = 0; i < Math.min(state.buttons().size(), prevState.buttons().size()); i++) {
                if (state.buttons().get(i) && !prevState.buttons().get(i)) {
                    return Optional.of(new JoystickButtonBind(joystick, i));
                }
            }

            for (int i = 0; i < Math.min(state.axes().size(), prevState.axes().size()); i++) {
                var axis = state.axes().get(i);
                var prevAxis = prevState.axes().get(i);
                var activationThreshold = joystick.config().buttonActivationThreshold;

                if (Math.abs(prevAxis) < activationThreshold) {
                    if (axis > activationThreshold) {
                        return Optional.of(new JoystickAxisBind(joystick, i, JoystickAxisBind.AxisDirection.POSITIVE));
                    } else if (axis < -activationThreshold) {
                        return Optional.of(new JoystickAxisBind(joystick, i, JoystickAxisBind.AxisDirection.NEGATIVE));
                    }
                }
            }

            for (int i = 0; i < Math.min(state.hats().size(), prevState.hats().size()); i++) {
                var hat = state.hats().get(i);
                var prevHat = prevState.hats().get(i);

                if (prevHat.isCentered() && !hat.isCentered()) {
                    return Optional.of(new JoystickHatBind(joystick, i, hat));
                }
            }

            return Optional.empty();
        }
    }
}
