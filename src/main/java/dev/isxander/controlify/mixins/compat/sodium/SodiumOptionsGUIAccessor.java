package dev.isxander.controlify.mixins.compat.sodium;

import me.jellysquid.mods.sodium.client.gui.SodiumOptionsGUI;
import me.jellysquid.mods.sodium.client.gui.options.OptionPage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(SodiumOptionsGUI.class)
public interface SodiumOptionsGUIAccessor {
    @Accessor
    List<OptionPage> getPages();

    @Accessor
    OptionPage getCurrentPage();
}
