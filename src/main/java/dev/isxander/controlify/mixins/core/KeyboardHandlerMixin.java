package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.*;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(KeyboardHandler.class)
public class KeyboardHandlerMixin {
    @Shadow @Final private Minecraft minecraft;

    @WrapOperation(
            method = "setup",
            at = @At(
                    value = "INVOKE",
                    target = "Lcom/mojang/blaze3d/platform/InputConstants;setupKeyboardCallbacks(Lcom/mojang/blaze3d/platform/Window;Lorg/lwjgl/glfw/GLFWKeyCallbackI;Lorg/lwjgl/glfw/GLFWCharCallbackI;Lorg/lwjgl/glfw/GLFWPreeditCallbackI;Lorg/lwjgl/glfw/GLFWIMEStatusCallbackI;)V"
            )
    )
    private void wrapKeyboardEvents(
            Window window,
            GLFWKeyCallbackI keyPressCallback,
            GLFWCharCallbackI charTypedCallback,
            GLFWPreeditCallbackI preeditCallback,
            GLFWIMEStatusCallbackI imeStatusCallback,
            Operation<Void> original
    ) {
        original.call(
                window,
                (GLFWKeyCallbackI) (w, k, s, a, m) -> {
                    onKeyboardInput();
                    keyPressCallback.invoke(w, k, s, a, m);
                },
                (GLFWCharCallbackI) (w, c) -> {
                    onKeyboardInput();
                    charTypedCallback.invoke(w, c);
                },
                (GLFWPreeditCallbackI) (w, pc, s, bc, bs, fb, c) -> {
                    onKeyboardInput();
                    preeditCallback.invoke(w, pc, s, bc, bs, fb, c);
                },
                imeStatusCallback
        );
    }

    @Unique
    private void onKeyboardInput() {
        minecraft.execute(() -> {
            if (Controlify.instance().currentInputMode() != InputMode.MIXED) {
                Controlify.instance().setInputMode(InputMode.KEYBOARD_MOUSE);
            }
        });
    }
}
