package dev.isxander.splitscreen.client.mixins.engine.reparent;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import dev.isxander.splitscreen.client.SplitscreenBootstrapper;
import net.minecraft.client.MouseHandler;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

@Mixin(MouseHandler.class)
public class MouseHandlerMixin {
    @ModifyExpressionValue(method = "grabMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;isWindowActive()Z"))
    private boolean shouldGrabMouse(boolean isWindowActive) {
        System.out.println("MouseHandlerMixin.shouldGrabMouse: isWindowActive=" + isWindowActive);
        return isWindowActive;
    }

    @ModifyExpressionValue(method = "releaseMouse", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MouseHandler;mouseGrabbed:Z", opcode = Opcodes.GETFIELD))
    private boolean shouldReleaseMouse(boolean mouseGrabbed) {
        System.out.println("MouseHandlerMixin.shouldReleaseMouse: mouseGrabbed=" + mouseGrabbed);
        return mouseGrabbed;
    }
}
