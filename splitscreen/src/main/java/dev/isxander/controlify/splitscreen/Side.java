package dev.isxander.controlify.splitscreen;

public enum Side {
    CONTROLLER,
    PAWN;

    public Side opposite() {
        return this == CONTROLLER ? PAWN : CONTROLLER;
    }
}
