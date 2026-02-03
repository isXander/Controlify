package dev.isxander.controlify.compatibility.yacl.mixins;

import dev.isxander.yacl3.gui.controllers.string.StringControllerElement;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StringControllerElement.class)
public interface StringControllerElementAccessor {
    @Invoker
    boolean callDoCopy();
}
