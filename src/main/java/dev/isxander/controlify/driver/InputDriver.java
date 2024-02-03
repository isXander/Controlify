package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.composable.ComposableControllerState;

public interface InputDriver extends Driver {

    ComposableControllerState getInputState();

    int numButtons();

    int numAxes();

    int numHats();

    boolean isGyroSupported();

    String getInputDriverDetails();

    InputDriver UNSUPPORTED = new InputDriver() {
        @Override
        public void update() {
        }

        @Override
        public ComposableControllerState getInputState() {
            return ComposableControllerState.EMPTY;
        }

        @Override
        public int numButtons() {
            return 0;
        }

        @Override
        public int numHats() {
            return 0;
        }

        @Override
        public int numAxes() {
            return 0;
        }

        @Override
        public boolean isGyroSupported() {
            return false;
        }

        @Override
        public String getInputDriverDetails() {
            return "Unsupported";
        }
    };
}
