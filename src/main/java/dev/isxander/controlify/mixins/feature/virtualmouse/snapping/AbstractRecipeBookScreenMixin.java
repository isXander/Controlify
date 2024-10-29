package dev.isxander.controlify.mixins.feature.virtualmouse.snapping;

import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.virtualmouse.SnapUtils;
import net.minecraft.client.gui.screens.inventory.*;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.HashSet;
import java.util.Set;

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
    /*@Shadow(remap = false, aliases = {"getRecipeBookComponent","m_5564_"})
    public abstract RecipeBookComponent getRecipeBookComponent();
    *///?}

    protected AbstractRecipeBookScreenMixin(Component title) {
        super(title);
    }

    @Override
    public Set<SnapPoint> getSnapPoints() {
        Set<SnapPoint> points = new HashSet<>(super.getSnapPoints());
        SnapUtils.addRecipeSnapPoints(
                //? if >=1.21.2 {
                recipeBookComponent,
                //?} else {
                /*getRecipeBookComponent(),
                *///?}
                points
        );
        return points;
    }
}
