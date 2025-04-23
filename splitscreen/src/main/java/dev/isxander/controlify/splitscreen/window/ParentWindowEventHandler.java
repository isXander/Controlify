package dev.isxander.controlify.splitscreen.window;

public interface ParentWindowEventHandler {
    ParentWindowEventHandler NO_OP = new ParentWindowEventHandler() {};

    default void onResizeParentWindow(int width, int height) {}

    default void onFocusParentWindow(boolean focused) {}
}
