package dev.isxander.controlify.mixins.feature.bind;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Gui.class)
public interface GuiAccessor {
    //? if >=26.2 {
    @Accessor("screen")
    void controlify$setScreenField(Screen screen);
    //?}
}
