package dev.isxander.controlify.driver.joystick;

import dev.isxander.controlify.driver.Driver;

public interface BasicJoystickInputDriver extends Driver {
    BasicJoystickState getBasicJoystickState();

    int getNumAxes();

    int getNumButtons();

    int getNumHats();

    String getBasicJoystickDetails();

    BasicJoystickInputDriver UNSUPPORTED = new BasicJoystickInputDriver() {
        @Override
        public void update() {

        }

        @Override
        public BasicJoystickState getBasicJoystickState() {
            return BasicJoystickState.EMPTY;
        }

        @Override
        public int getNumAxes() {
            return 0;
        }

        @Override
        public int getNumButtons() {
            return 0;
        }

        @Override
        public int getNumHats() {
            return 0;
        }

        @Override
        public String getBasicJoystickDetails() {
            return "UNSUPPORTED";
        }
    };
}
