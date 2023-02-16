package dev.isxander.controlify.controller;

import java.util.List;
import java.util.Set;

public interface ControllerState {
    List<Float> axes();
    List<Float> rawAxes();

    List<Boolean> buttons();

    boolean hasAnyInput();

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
        public boolean hasAnyInput() {
            return false;
        }
    };
}
