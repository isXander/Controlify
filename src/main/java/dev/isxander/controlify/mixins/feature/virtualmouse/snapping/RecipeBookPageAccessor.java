package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RecipeBookPage.class)
public interface RecipeBookPageAccessor {
    @Accessor
    List<RecipeButton> getButtons();

    //? if >=1.21.11 {
    @Accessor
    net.minecraft.client.gui.components.ImageButton getForwardButton();

    @Accessor
    net.minecraft.client.gui.components.ImageButton getBackButton();
    //?} else {
    /*@Accessor
    net.minecraft.client.gui.components.StateSwitchingButton getForwardButton();

    @Accessor
    net.minecraft.client.gui.components.StateSwitchingButton getBackButton();
    *///?}
}
