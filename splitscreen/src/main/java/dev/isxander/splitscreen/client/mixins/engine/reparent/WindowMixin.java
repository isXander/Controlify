package dev.isxander.splitscreen.client.mixins.engine.reparent;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.TracyFrameCapture;
import com.mojang.blaze3d.platform.VideoMode;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.platform.WindowEventHandler;
import dev.isxander.splitscreen.client.ControllerBridge;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ReparentingHostSplitscreenEngine;
import dev.isxander.splitscreen.client.engine.impl.reparenting.ReparentingSplitscreenEngine;
import dev.isxander.splitscreen.client.engine.impl.reparenting.events.VanillaWindowFocusEvent;
import dev.isxander.splitscreen.client.engine.impl.reparenting.parent.ParentWindow;
import org.lwjgl.glfw.GLFWImage;
import org.slf4j.Logger;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Optional;
import java.util.function.Function;

@Mixin(Window.class)
public class WindowMixin {
    @Shadow @Final private static Logger LOGGER;

    @Shadow
    @Final
    private long window;
    @Unique private boolean hasDoneInitialSetup = false;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void markInitialSetup(CallbackInfo ci) {
        // Just a bug check to ensure things aren't going wrong preventing window from setting up correctly.
        this.hasDoneInitialSetup = true;
    }

    /**
     * Always transfer focus to the controller's window.
     */
    @WrapOperation(method = "onFocus", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/WindowEventHandler;setWindowActive(Z)V"))
    private void transferFocusToController(WindowEventHandler instance, boolean hasFocus, Operation<Void> original) {
        VanillaWindowFocusEvent.EVENT.invoker().onFocus((Window) (Object) this, hasFocus);
    }

    /**
     * Propagate the icon of the controller's window to the parent window.
     * @param iconBuffer the icon buffer to set
     * @return icon buffer to give to child window
     */
    @ModifyArg(method = "setIcon", at = @At(value = "INVOKE", target = "Lorg/lwjgl/glfw/GLFW;glfwSetWindowIcon(JLorg/lwjgl/glfw/GLFWImage$Buffer;)V"))
    private GLFWImage.Buffer propagateWindowIconToParent(GLFWImage.Buffer iconBuffer) {
        SplitscreenBootstrapper.getController().flatMap(ReparentingHostSplitscreenEngine::tryGet).ifPresent(engine -> {
            Optional.ofNullable(engine.getParentWindow()).ifPresent(win -> win.setIcon(iconBuffer));
        });

        return iconBuffer;
    }

    /**
     * Propagate the title of the controller's window to the parent window.
     * @param title the title to set
     */
    @Inject(method = "setTitle", at = @At("HEAD"))
    private void propagateTitleToParent(String title, CallbackInfo ci) {
        SplitscreenBootstrapper.getController().flatMap(ReparentingHostSplitscreenEngine::tryGet).ifPresent(engine -> {
            Optional.ofNullable(engine.getParentWindow()).ifPresent(win -> win.setTitle(title));
        });
    }

//    /**
//     * GLFW does not correctly report the window focus state when the window is a child.
//     * Instead, the controller will propagate this event from the parent window.
//     * @param instance receiver
//     * @param glfwReportedFocus the incorrect focus state reported by GLFW
//     * @return if the focus state should be set by GLFW child window
//     */
//    @WrapWithCondition(method = "onFocus", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/WindowEventHandler;setWindowActive(Z)V"))
//    private boolean dontListenToChildWindowForFocus(WindowEventHandler instance, boolean glfwReportedFocus) {
//        return !SplitscreenBootstrapper.isSplitscreen();
//    }

    @Inject(method = "onEnter", at = @At("HEAD"))
    private void giveSelfFocusIfForeground(long window, boolean cursorEntered, CallbackInfo ci) {
        if (window == this.window && cursorEntered) {
            SplitscreenBootstrapper.getControllerBridge().ifPresent(ControllerBridge::giveFocusToMeIfForeground);
        }
    }

    /*
     * We can't allow for child windows to become fullscreen on their own accord.
     * This will blow everything up.
     */
    @WrapMethod(method = "setMode")
    private void preventModeChange(Operation<Void> original) {
        preventIfSplitscreen(original::call);
    }
    @WrapMethod(method = "changeFullscreenVideoMode")
    private void preventChangeFullscreenVideoMode(Operation<Void> original) {
        preventIfSplitscreen(original::call);
    }
    @WrapMethod(method = "setPreferredFullscreenVideoMode")
    private void preventSetPreferredFullscreenVideoMode(Optional<VideoMode> preferredFullscreenVideoMode, Operation<Void> original) {
        preventIfSplitscreen(() -> original.call(preferredFullscreenVideoMode));
    }
    @WrapMethod(method = "toggleFullScreen")
    private void preventToggleFullScreen(Operation<Void> original) {
        reparentingEngine()
                .flatMap(filterHost())
                .flatMap(hostEngine -> Optional.ofNullable(hostEngine.getParentWindow()))
                .ifPresentOrElse(
                        ParentWindow::toggleFullscreen,
                        original::call
                );
    }
    @WrapMethod(method = "setWindowed")
    private void preventSetWindowed(int windowedWidth, int windowedHeight, Operation<Void> original) {
        reparentingEngine()
                .flatMap(filterHost())
                .flatMap(hostEngine -> Optional.ofNullable(hostEngine.getParentWindow()))
                .ifPresentOrElse(
                        window -> {
                            window.setWindowed(windowedWidth, windowedHeight);
                        },
                        original::call
                );
    }
    @WrapMethod(method = "updateFullscreen")
    private void preventUpdateFullscreen(boolean vsyncEnabled, TracyFrameCapture tracyFrameCapture, Operation<Void> original) {
        preventIfSplitscreen(() -> original.call(vsyncEnabled, tracyFrameCapture));
    }

    @ModifyReturnValue(method = "isFullscreen", at = @At("RETURN"))
    private boolean injectParentStateToFullscreenCheck(boolean childIsFullscreen) {
        return reparentingEngine()
                .flatMap(filterHost())
                .flatMap(hostEngine -> Optional.ofNullable(hostEngine.getParentWindow()))
                .map(ParentWindow::isFullscreen)
                .orElse(childIsFullscreen);
    }

    @Unique
    private Optional<ReparentingSplitscreenEngine> reparentingEngine() {
        return SplitscreenBootstrapper.getEngine()
                .flatMap(engine -> engine instanceof ReparentingSplitscreenEngine reparenting ? Optional.of(reparenting) : Optional.empty());
    }

    @Unique
    private Function<ReparentingSplitscreenEngine, Optional<ReparentingHostSplitscreenEngine>> filterHost() {
        return engine -> engine instanceof ReparentingHostSplitscreenEngine reparenting ? Optional.of(reparenting) : Optional.empty();
    }

    @Unique
    private void preventIfSplitscreen(Runnable originalCall) {
        if (SplitscreenBootstrapper.getEngine().map(engine -> engine instanceof ReparentingSplitscreenEngine).orElse(false)) {
            if (!hasDoneInitialSetup) {
                originalCall.run();
                return;
            }

            LOGGER.info("Preventing fullscreen mode change in child window");
            return;
        }

        originalCall.run();
    }
}
