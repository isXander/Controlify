package dev.isxander.controlify.virtualmouse;

import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookComponentAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookPageAccessor;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.joml.Vector2i;

import java.util.Collection;

public final class SnapUtils {
    private SnapUtils() {
    }

    public static void addRecipeSnapPoints(RecipeBookComponent recipeBookComponent, Collection<SnapPoint> points) {
        if (recipeBookComponent.isVisible()) {
            RecipeBookComponentAccessor componentAccessor = (RecipeBookComponentAccessor) recipeBookComponent;
            componentAccessor.getTabButtons().forEach(button -> {
                int x = button.getX() + button.getWidth() / 2;
                int y = button.getY() + button.getHeight() / 2;
                points.add(new SnapPoint(new Vector2i(x, y), 20));
            });

            RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) componentAccessor.getRecipeBookPage();
            pageAccessor.getButtons().forEach(button -> {
                int x = button.getX() + button.getWidth() / 2;
                int y = button.getY() + button.getHeight() / 2;
                points.add(new SnapPoint(new Vector2i(x, y), 21));
            });
        }
    }
}
