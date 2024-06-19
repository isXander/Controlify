package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import net.minecraft.client.gui.screens./*? if >1.20.6 >>*//*options.*/ OptionsSubScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(OptionsSubScreen.class)
public interface OptionsSubScreenAccessor {
    @Accessor
    Screen getLastScreen();
}
