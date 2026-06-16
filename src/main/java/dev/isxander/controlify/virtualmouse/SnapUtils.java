package dev.isxander.controlify.virtualmouse;

import dev.isxander.controlify.api.vmousesnapping.SnapPoint;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookComponentAccessor;
import dev.isxander.controlify.mixins.feature.virtualmouse.snapping.RecipeBookPageAccessor;
import net.minecraft.client.gui.screens.recipebook.RecipeBookComponent;
import org.joml.Vector2i;

import java.util.function.Consumer;

public final class SnapUtils {
    private SnapUtils() {
    }

    public static void addRecipeSnapPoints(RecipeBookComponent recipeBookComponent, Consumer<SnapPoint> consumer) {
        if (recipeBookComponent.isVisible()) {
            RecipeBookComponentAccessor componentAccessor = (RecipeBookComponentAccessor) recipeBookComponent;
            componentAccessor.controlify$getTabButtons().forEach(button -> {
                int x = button.getX() + button.getWidth() / 2;
                int y = button.getY() + button.getHeight() / 2;
                consumer.accept(new SnapPoint(new Vector2i(x, y), 20));
            });

            var filterButton = componentAccessor.controlify$getFilterButton();
            if (filterButton.visible) {
                int x = filterButton.getX() + filterButton.getWidth() / 2;
                int y = filterButton.getY() + filterButton.getHeight() / 2;
                consumer.accept(new SnapPoint(new Vector2i(x, y), 14));
            }

            RecipeBookPageAccessor pageAccessor = (RecipeBookPageAccessor) componentAccessor.controlify$getRecipeBookPage();
            pageAccessor.controlify$getButtons().forEach(button -> {
                int x = button.getX() + button.getWidth() / 2;
                int y = button.getY() + button.getHeight() / 2;
                consumer.accept(new SnapPoint(new Vector2i(x, y), 21));
            });

            var forwardButton = pageAccessor.controlify$getForwardButton();
            if (forwardButton.visible) {
                int x = forwardButton.getX() + forwardButton.getWidth() / 2 - 2;
                int y = forwardButton.getY() + forwardButton.getHeight() / 2;
                consumer.accept(new SnapPoint(new Vector2i(x, y), 10));
            }

            var backButton = pageAccessor.controlify$getBackButton();
            if (backButton.visible) {
                int x = backButton.getX() + backButton.getWidth() / 2 + 2;
                int y = backButton.getY() + backButton.getHeight() / 2;
                consumer.accept(new SnapPoint(new Vector2i(x, y), 10));
            }
        }
    }
}
