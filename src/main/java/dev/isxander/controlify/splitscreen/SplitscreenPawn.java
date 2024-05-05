package dev.isxander.controlify.splitscreen;

import dev.isxander.controlify.splitscreen.window.SplitscreenPosition;

public interface SplitscreenPawn {
    void joinMyServer(int port);

    void configureSplitscreen(long monitorIndex, SplitscreenPosition position);
}
