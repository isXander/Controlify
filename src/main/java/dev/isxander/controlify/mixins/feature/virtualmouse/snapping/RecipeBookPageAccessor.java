package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import net.minecraft.client.gui.components.ImageButton;
import net.minecraft.client.gui.screens.recipebook.RecipeBookPage;
import net.minecraft.client.gui.screens.recipebook.RecipeButton;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(RecipeBookPage.class)
public interface RecipeBookPageAccessor {
    @Accessor("buttons")
    List<RecipeButton> controlify$getButtons();

    @Accessor("forwardButton")
    ImageButton controlify$getForwardButton();

    @Accessor("backButton")
    ImageButton controlify$getBackButton();
}
