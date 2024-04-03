package dev.isxander.controlify.mixins.compat.sodium;

import org.spongepowered.asm.mixin.Mixin;

/*? if sodium {*//*
import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import me.jellysquid.mods.sodium.client.gui.options.control.ControlElement;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Pseudo
@Mixin(value = SodiumOptionsGUI.class, remap = false)
public interface SodiumOptionsGUIAccessor {
    @Accessor
    List<OptionPage> getPages();

    @Accessor
    OptionPage getCurrentPage();

    @Accessor
    List<ControlElement<?>> getControls();
}
*//*?} else {*/
@Mixin(targets = {})
public interface SodiumOptionsGUIAccessor {
}
/*?}*/
