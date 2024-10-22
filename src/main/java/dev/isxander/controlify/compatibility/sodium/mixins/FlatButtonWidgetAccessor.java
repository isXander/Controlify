//? if sodium {
package dev.isxander.controlify.compatibility.sodium.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import /*$ sodium-package >>*/ net.caffeinemc.mods.sodium .client.gui.widgets.FlatButtonWidget;

@Mixin(FlatButtonWidget.class)
public interface FlatButtonWidgetAccessor {
    @Invoker
    void invokeDoAction();
}
//?}
