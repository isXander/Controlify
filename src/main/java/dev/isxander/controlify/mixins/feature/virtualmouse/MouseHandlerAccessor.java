package dev.isxander.controlify.mixins.feature.virtualmouse;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Invoker
    void invokeOnMove(long window, double x, double y);

    @Invoker
    void invokeOnPress(long window, int button, int action, int modifiers);

    @Invoker
    void invokeOnScroll(long window, double scrollDeltaX, double scrollDeltaY);
}
