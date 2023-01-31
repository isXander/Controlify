package dev.isxander.controlify.controller;

public record ControllerState(AxesState axes, ButtonState buttons) {
    public static final ControllerState EMPTY = new ControllerState(AxesState.EMPTY, ButtonState.EMPTY);

    public boolean hasAnyInput() {
        return !this.equals(EMPTY);
    }
}
