package dev.isxander.splitscreen;

/**
 * Represents the side of the connection.
 */
public enum Side {
    CONTROLLER,
    PAWN;

    public Side opposite() {
        return this == CONTROLLER ? PAWN : CONTROLLER;
    }
}
