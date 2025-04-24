package dev.isxander.controlify.splitscreen.server;

import dev.isxander.controlify.splitscreen.ControllerBridge;
import dev.isxander.controlify.splitscreen.SplitscreenPawn;
import dev.isxander.controlify.splitscreen.window.manager.NativeWindowHandle;
import dev.isxander.controlify.splitscreen.window.manager.WindowManager;
import net.minecraft.client.Minecraft;

public class LocalControllerBridge implements ControllerBridge {

    private final Minecraft minecraft;
    private final SplitscreenController controller;

    public LocalControllerBridge(Minecraft minecraft, SplitscreenController controller) {
        this.minecraft = minecraft;
        this.controller = controller;
    }

    @Override
    public void giveFocusToMeIfForeground() {
        this.giveFocusToChildIfForeground(
                WindowManager.get().getNativeWindowHandle(
                        minecraft.getWindow().getWindow()
                ),
                controller.getLocalPawn()
        );
    }

    public void giveFocusToChildIfForeground(NativeWindowHandle childWindow, SplitscreenPawn childPawn) {
        long glfwParentWindowHandle = controller.getParentWindow().getGlfwWindowHandle();
        NativeWindowHandle nativeParentWindowHandle = WindowManager.get().getNativeWindowHandle(glfwParentWindowHandle);

        if (WindowManager.get().giveChildFocusIfParentIsForeground(nativeParentWindowHandle, childWindow)) {
            this.controller.forEachPawn(pawn -> {
                pawn.setWindowFocusState(pawn == childPawn);
            });
        }
    }
}
