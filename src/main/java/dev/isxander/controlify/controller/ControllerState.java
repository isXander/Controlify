package dev.isxander.controlify.controller;

import java.util.List;

public interface ControllerState {
    List<Float> axes();
    List<Float> rawAxes();

    List<Boolean> buttons();

    boolean shouldSwitchTo();

    ControllerState EMPTY = new ControllerState() {
        @Override
        public List<Float> axes() {
            return List.of();
        }

        @Override
        public List<Float> rawAxes() {
            return List.of();
        }

        @Override
        public List<Boolean> buttons() {
            return List.of();
        }

        @Override
        public boolean shouldSwitchTo() {
            return false;
        }
    };
}
