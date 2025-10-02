package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.utils.MouseMinecraftCallNotifier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import org.lwjgl.glfw.GLFWCursorPosCallbackI;
import org.lwjgl.glfw.GLFWDropCallbackI;
import org.lwjgl.glfw.GLFWMouseButtonCallbackI;
import org.lwjgl.glfw.GLFWScrollCallbackI;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin implements MouseMinecraftCallNotifier {
    @Shadow @Final private Minecraft minecraft;

    @Unique private boolean controlify$calledFromMinecraftSetScreen = false;

    @WrapOperation(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/InputConstants;setupMouseCallbacks(Lcom/mojang/blaze3d/platform/Window;Lorg/lwjgl/glfw/GLFWCursorPosCallbackI;Lorg/lwjgl/glfw/GLFWMouseButtonCallbackI;Lorg/lwjgl/glfw/GLFWScrollCallbackI;Lorg/lwjgl/glfw/GLFWDropCallbackI;)V"
            )
    )
    private void wrapMouseEvents(
            Window window,
            GLFWCursorPosCallbackI moveCallback,
            GLFWMouseButtonCallbackI buttonCallback,
            GLFWScrollCallbackI scrollCallback,
            GLFWDropCallbackI fileDropCallback,
            Operation<Void> operation
    ) {
        operation.call(
                window,
                (GLFWCursorPosCallbackI) (w, x, y) -> {
                    onMouse(w);
                    moveCallback.invoke(w, x, y);
                },
                (GLFWMouseButtonCallbackI) (w, b, a, m) -> {
                    onMouse(w);
                    buttonCallback.invoke(w, b, a, m);
                },
                (GLFWScrollCallbackI) (w, dx, dy) -> {
                    onMouse(w);
                    scrollCallback.invoke(w, dx, dy);
                },
                fileDropCallback
        );
    }

    @Unique
    private void onMouse(long window) {
        //? if >=1.21.9 {
        var windowHandle = minecraft.getWindow().handle();
        //?} else {
        /*var windowHandle = minecraft.getWindow().getWindow();
        *///?}
        if (window == windowHandle) {
            minecraft.execute(() -> {
                if (Controlify.instance().currentInputMode() != InputMode.MIXED) {
                    Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
                } else {
                    Controlify.instance().showCursorTemporarily();
                }
            });
        }
    }

    /**
     * Without this, mouse is left in the center of the screen that conflicts with controller focus.
     */
    @Inject(
            method = "releaseMouse",
            at = @At(
                    value = "INVOKE",
                    //? if >=1.21.9 {
                    target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(Lcom/mojang/blaze3d/platform/Window;IDD)V"
                    //?} else {
                    /*target = "Lcom/mojang/blaze3d/platform/InputConstants;grabOrReleaseMouse(JIDD)V"
                    *///?}
            )
    )
    private void moveMouseIfNecessary(CallbackInfo ci) {
        if (!controlify$calledFromMinecraftSetScreen && ControlifyApi.get().currentInputMode().isController()) {
            Controlify.instance().hideMouse(true, true);
        }
    }

    // shift after RETURN to escape the if statement scope
    @Inject(method = "releaseMouse", at = @At(value = "RETURN"))
    private void resetCalledFromMinecraftSetScreen(CallbackInfo ci) {
        controlify$calledFromMinecraftSetScreen = false;
    }

    @ModifyExpressionValue(method = "grabMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"))
    private boolean passWindowActiveCheckIfOOFInputIsOn(boolean isWindowActive) {
        return isWindowActive || (ControlifyApi.get().currentInputMode().isController() && Controlify.instance().config().globalSettings().outOfFocusInput);
    }

    @Override
    public void imFromMinecraftSetScreen() {
        controlify$calledFromMinecraftSetScreen = true;
    }
}
