package dev.isxander.controlify.mixins.feature.virtualmouse;

import net.minecraft.client.KeyboardHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(KeyboardHandler.class)
public interface KeyboardHandlerAccessor {
    @Invoker
    void invokeKeyPress(long window, int key, int scancode, int action, int modifiers);
}
