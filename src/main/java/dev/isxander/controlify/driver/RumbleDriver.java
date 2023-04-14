package dev.isxander.controlify.driver;

public interface RumbleDriver extends Driver {
    boolean rumble(float strongMagnitude, float weakMagnitude);

    boolean isRumbleSupported();

    RumbleDriver UNSUPPORTED = new RumbleDriver() {
        @Override
        public void update() {
        }

        @Override
        public boolean rumble(float strongMagnitude, float weakMagnitude) {
            return false;
        }

        @Override
        public boolean isRumbleSupported() {
            return false;
        }
    };
}
