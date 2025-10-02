package dev.isxander.controlify.mixins.feature.virtualmouse;

import net.minecraft.client.MouseHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(MouseHandler.class)
public interface MouseHandlerAccessor {
    @Invoker
    void invokeOnMove(long window, double x, double y);

    //? if >=1.21.9 {
    @Invoker
    void invokeOnButton(long l, net.minecraft.client.input.MouseButtonInfo mouseButtonInfo, int i);
    //?} else {
    /*@Invoker
    void invokeOnPress(long window, int button, int action, int modifiers);
    *///?}

    @Invoker
    void invokeOnScroll(long window, double scrollDeltaX, double scrollDeltaY);

    @Accessor
    void setMouseGrabbed(boolean mouseGrabbed);
}
