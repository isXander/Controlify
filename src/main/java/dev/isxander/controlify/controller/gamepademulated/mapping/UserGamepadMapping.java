package dev.isxander.controlify.controller.gamepademulated.mapping;

import dev.isxander.controlify.controller.gamepad.GamepadState;
import dev.isxander.controlify.driver.gamepad.BasicGamepadState;
import dev.isxander.controlify.driver.joystick.BasicJoystickState;

public record UserGamepadMapping(
        ButtonMapping faceDownButton,
        ButtonMapping faceLeftButton,
        ButtonMapping faceRightButton,
        ButtonMapping faceUpButton,

        ButtonMapping leftBumper,
        ButtonMapping rightBumper,

        ButtonMapping leftSpecial,
        ButtonMapping rightSpecial,

        ButtonMapping leftStickDown,
        ButtonMapping rightStickDown,

        ButtonMapping dpadUp,
        ButtonMapping dpadLeft,
        ButtonMapping dpadDown,
        ButtonMapping dpadRight,

        AxisMapping leftStickX,
        AxisMapping leftStickY,
        AxisMapping rightStickX,
        AxisMapping rightStickY,

        AxisMapping triggerLeft,
        AxisMapping triggerRight
) implements GamepadMapping {
    @Override
    public BasicGamepadState mapJoystick(BasicJoystickState state) {
        return new BasicGamepadState(
                new GamepadState.AxesState(
                        leftStickX.mapAxis(state),
                        leftStickY.mapAxis(state),
                        rightStickX.mapAxis(state),
                        rightStickY.mapAxis(state),

                        triggerLeft.mapAxis(state),
                        triggerRight.mapAxis(state)
                ),
                new GamepadState.ButtonState(
                        faceDownButton.mapButton(state),
                        faceRightButton.mapButton(state),
                        faceLeftButton.mapButton(state),
                        faceUpButton.mapButton(state),

                        leftBumper.mapButton(state),
                        rightBumper.mapButton(state),

                        leftSpecial.mapButton(state),
                        rightSpecial.mapButton(state),
                        false,

                        dpadUp.mapButton(state),
                        dpadDown.mapButton(state),
                        dpadLeft.mapButton(state),
                        dpadRight.mapButton(state),

                        leftStickDown.mapButton(state),
                        rightStickDown.mapButton(state)
                )
        );
    }

    public static final UserGamepadMapping NO_MAPPING = new Builder().build();

    public static class Builder {
        private ButtonMapping faceDownButton = new ButtonMapping.FromNothing(false);
        private ButtonMapping faceLeftButton = new ButtonMapping.FromNothing(false);
        private ButtonMapping faceRightButton = new ButtonMapping.FromNothing(false);
        private ButtonMapping faceUpButton = new ButtonMapping.FromNothing(false);

        private ButtonMapping leftBumper = new ButtonMapping.FromNothing(false);
        private ButtonMapping rightBumper = new ButtonMapping.FromNothing(false);

        private ButtonMapping leftSpecial = new ButtonMapping.FromNothing(false);
        private ButtonMapping rightSpecial = new ButtonMapping.FromNothing(false);

        private ButtonMapping leftStickDown = new ButtonMapping.FromNothing(false);
        private ButtonMapping rightStickDown = new ButtonMapping.FromNothing(false);

        private ButtonMapping dpadUp = new ButtonMapping.FromNothing(false);
        private ButtonMapping dpadLeft = new ButtonMapping.FromNothing(false);
        private ButtonMapping dpadDown = new ButtonMapping.FromNothing(false);
        private ButtonMapping dpadRight = new ButtonMapping.FromNothing(false);

        private AxisMapping leftStickX = new AxisMapping.FromNothing(0);
        private AxisMapping leftStickY = new AxisMapping.FromNothing(0);
        private AxisMapping rightStickX = new AxisMapping.FromNothing(0);
        private AxisMapping rightStickY = new AxisMapping.FromNothing(0);

        private AxisMapping triggerLeft = new AxisMapping.FromNothing(0);
        private AxisMapping triggerRight = new AxisMapping.FromNothing(0);

        public Builder faceDownButton(ButtonMapping faceDownButton) {
            this.faceDownButton = faceDownButton;
            return this;
        }

        public Builder faceLeftButton(ButtonMapping faceLeftButton) {
            this.faceLeftButton = faceLeftButton;
            return this;
        }

        public Builder faceRightButton(ButtonMapping faceRightButton) {
            this.faceRightButton = faceRightButton;
            return this;
        }

        public Builder faceUpButton(ButtonMapping faceUpButton) {
            this.faceUpButton = faceUpButton;
            return this;
        }

        public Builder leftBumper(ButtonMapping leftBumper) {
            this.leftBumper = leftBumper;
            return this;
        }

        public Builder rightBumper(ButtonMapping rightBumper) {
            this.rightBumper = rightBumper;
            return this;
        }

        public Builder leftSpecial(ButtonMapping leftSpecial) {
            this.leftSpecial = leftSpecial;
            return this;
        }

        public Builder rightSpecial(ButtonMapping rightSpecial) {
            this.rightSpecial = rightSpecial;
            return this;
        }

        public Builder leftStickDown(ButtonMapping leftStickDown) {
            this.leftStickDown = leftStickDown;
            return this;
        }

        public Builder rightStickDown(ButtonMapping rightStickDown) {
            this.rightStickDown = rightStickDown;
            return this;
        }

        public Builder dpadUp(ButtonMapping dpadUp) {
            this.dpadUp = dpadUp;
            return this;
        }

        public Builder dpadLeft(ButtonMapping dpadLeft) {
            this.dpadLeft = dpadLeft;
            return this;
        }

        public Builder dpadDown(ButtonMapping dpadDown) {
            this.dpadDown = dpadDown;
            return this;
        }

        public Builder dpadRight(ButtonMapping dpadRight) {
            this.dpadRight = dpadRight;
            return this;
        }

        public Builder leftStickX(AxisMapping leftStickX) {
            this.leftStickX = leftStickX;
            return this;
        }

        public Builder leftStickY(AxisMapping leftStickY) {
            this.leftStickY = leftStickY;
            return this;
        }

        public Builder rightStickX(AxisMapping rightStickX) {
            this.rightStickX = rightStickX;
            return this;
        }

        public Builder rightStickY(AxisMapping rightStickY) {
            this.rightStickY = rightStickY;
            return this;
        }

        public Builder triggerLeft(AxisMapping triggerLeft) {
            this.triggerLeft = triggerLeft;
            return this;
        }

        public Builder triggerRight(AxisMapping triggerRight) {
            this.triggerRight = triggerRight;
            return this;
        }

        public UserGamepadMapping build() {
            return new UserGamepadMapping(
                    faceDownButton,
                    faceLeftButton,
                    faceRightButton,
                    faceUpButton,

                    leftBumper,
                    rightBumper,

                    leftSpecial,
                    rightSpecial,

                    leftStickDown,
                    rightStickDown,

                    dpadUp,
                    dpadLeft,
                    dpadDown,
                    dpadRight,

                    leftStickX,
                    leftStickY,
                    rightStickX,
                    rightStickY,

                    triggerLeft,
                    triggerRight
            );
        }
    }
}
