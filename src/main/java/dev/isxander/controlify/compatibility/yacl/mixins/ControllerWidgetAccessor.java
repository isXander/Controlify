package dev.isxander.controlify.compatibility.yacl.mixins;

import dev.isxander.yacl3.api.Controller;
import dev.isxander.yacl3.gui.controllers.ControllerWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = ControllerWidget.class, remap = false)
public interface ControllerWidgetAccessor {
    @Accessor
    Controller<?> getControl();
}
