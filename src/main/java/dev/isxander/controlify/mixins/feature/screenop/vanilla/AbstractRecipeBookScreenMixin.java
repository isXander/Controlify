package dev.isxander.controlify.mixins.feature.screenop.vanilla;

import dev.isxander.controlify.screenop.ScreenProcessor;
import dev.isxander.controlify.screenop.ScreenProcessorProvider;
import dev.isxander.controlify.screenop.compat.vanilla.RecipeBookScreenProcessor;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.client.gui.screens.recipebook.RecipeUpdateListener;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;

//? if >=1.21.2 {
@Mixin(AbstractRecipeBookScreen.class)
//?} else {
/*@Mixin(value = {
        InventoryScreen.class,
        AbstractFurnaceScreen.class,
        CraftingScreen.class
})
*///?}
public abstract class AbstractRecipeBookScreenMixin
        extends AbstractContainerScreenMixin
        implements ScreenProcessorProvider, RecipeUpdateListener, RecipeBookScreenProcessor.RecipeBookScreenAccessor {

    @Unique
    private final RecipeBookScreenProcessor<?> processor =
            new RecipeBookScreenProcessor<>(
                    /*? if >=1.21.2 {*/ (AbstractRecipeBookScreen<?>) /*?} else {*/ /*(AbstractContainerScreen<?>) *//*?}*/ (Object) this,
                    this,
                    () -> hoveredSlot,
                    this::slotClicked,
                    this::handleControllerItemSlotActions
            );

    //? if >=1.21.2 {
    @Shadow
    @Final
    private RecipeBookComponent<?> recipeBookComponent;

    @Unique
    private RecipeBookComponent<?> getRecipeBookComponent() {
        return recipeBookComponent;
    }
    //?}

    @Override
    public RecipeBookComponent/*? if >=1.21.2 >>*/<?> controlify$getRecipeBookComponent() {
        return this.getRecipeBookComponent();
    }

    @Override
    public ScreenProcessor<?> screenProcessor() {
        return processor;
    }
}
