package dev.isxander.controlify.mixins.core;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.platform.Window;
import dev.isxander.controlify.Controlify;
import dev.isxander.controlify.InputMode;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFWCharModsCallbackI;
import org.lwjgl.glfw.GLFWKeyCallbackI;
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
                    //? if >=1.21.9 {
                    target = "Lcom/mojang/blaze3d/platform/InputConstants;setupKeyboardCallbacks(Lcom/mojang/blaze3d/platform/Window;Lorg/lwjgl/glfw/GLFWKeyCallbackI;Lorg/lwjgl/glfw/GLFWCharModsCallbackI;)V"
                    //?} else {
                    /*target = "Lcom/mojang/blaze3d/platform/InputConstants;setupKeyboardCallbacks(JLorg/lwjgl/glfw/GLFWKeyCallbackI;Lorg/lwjgl/glfw/GLFWCharModsCallbackI;)V"
                    *///?}
            )
    )
    private void wrapKeyboardEvents(
            /*? if >=1.21.9 {*/ Window /*?} else {*/ /*long *//*?}*/ window,
            GLFWKeyCallbackI keyCallback,
            GLFWCharModsCallbackI charCallback,
            Operation<Void> original
    ) {
        original.call(
                window,
                (GLFWKeyCallbackI) (w, k, s, a, m) -> {
                    onKeyboardInput();
                    keyCallback.invoke(w, k, s, a, m);
                },
                (GLFWCharModsCallbackI) (w, c, m) -> {
                    onKeyboardInput();
                    charCallback.invoke(w, c, m);
                }
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
