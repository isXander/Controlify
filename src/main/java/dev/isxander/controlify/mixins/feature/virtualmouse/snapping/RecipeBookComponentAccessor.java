package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeBookTabButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RecipeBookComponent.class)
public interface RecipeBookComponentAccessor {
    @Accessor
    RecipeBookPage getRecipeBookPage();

    @Accessor
    List<RecipeBookTabButton> getTabButtons();
}
