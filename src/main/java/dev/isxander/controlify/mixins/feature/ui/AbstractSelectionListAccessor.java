package dev.isxander.controlify.mixins.feature.ui;

import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(AbstractSelectionList.class)
public interface AbstractSelectionListAccessor {
    @Accessor("MENU_LIST_BACKGROUND")
    static ResourceLocation getMenuListBackground() {
        throw new AssertionError();
    }

    @Accessor("INWORLD_MENU_LIST_BACKGROUND")
    static ResourceLocation getInWorldMenuListBackground() {
        throw new AssertionError();
    }
}
