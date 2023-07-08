package dev.isxander.controlify.mixins.feature.bind;

import dev.isxander.controlify.bindings.InputHandledEvent;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    // KeyboardHandler and MouseHandler run input events through Minecraft#execute,
    // which is polled by the injection point below. Cannot be done in normal tick event
    // as that could run up to 10 times per frame depending on framerate.
    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Minecraft;runAllTasks()V", shift = At.Shift.AFTER))
    private void onTasksExecuted(boolean tick, CallbackInfo ci) {
        InputHandledEvent.EVENT.invoker().onInputHandled();
    }
}
