package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.virtualmouse.SnapUtils;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Consumer;

//? if >=1.21.2 {
@Mixin(AbstractRecipeBookScreen.class)
//?} else {
/*@Mixin(value = {
        InventoryScreen.class,
        AbstractFurnaceScreen.class,
        CraftingScreen.class
})
*///?}
public abstract class AbstractRecipeBookScreenMixin<T extends AbstractContainerMenu>
        extends AbstractContainerScreenMixin<T> {

    //? if >=1.21.2 {
    @Shadow @Final private RecipeBookComponent<?> recipeBookComponent;
    //?} else {
    /*@Shadow(aliases = {"getRecipeBookComponent","m_5564_","method_2659"})
    public abstract RecipeBookComponent getRecipeBookComponent();
    *///?}

    @Override
    public void controlify$collectSnapPoints(Consumer<SnapPoint> consumer) {
        super.controlify$collectSnapPoints(consumer);

        SnapUtils.addRecipeSnapPoints(
                //? if >=1.21.2 {
                recipeBookComponent,
                //?} else {
                /*getRecipeBookComponent(),
                *///?}
                consumer
        );
    }
}
