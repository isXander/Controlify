package dev.isxander.controlify.driver;

import dev.isxander.controlify.controller.BatteryLevel;

public interface BatteryDriver extends Driver {
    BatteryLevel getBatteryLevel();

    String getBatteryDriverDetails();

    BatteryDriver UNSUPPORTED = new BatteryDriver() {
        @Override
        public void update() {
        }

        @Override
        public BatteryLevel getBatteryLevel() {
            return BatteryLevel.UNKNOWN;
        }

        @Override
        public String getBatteryDriverDetails() {
            return "Unsupported";
        }
    };
}
