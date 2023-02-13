package dev.isxander.controlify.controller;

public record ControllerState(AxesState axes, AxesState rawAxes, ButtonState buttons) {
    public static final ControllerState EMPTY = new ControllerState(AxesState.EMPTY, AxesState.EMPTY, ButtonState.EMPTY);

    public boolean hasAnyInput() {
        return !this.axes().equals(AxesState.EMPTY) || !this.buttons().equals(ButtonState.EMPTY);
    }
}
