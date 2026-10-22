package dev.isxander.controlify.mixins.feature.screenop.impl.container;

import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CreativeModeInventoryScreen.class)
public interface CreativeModeInventoryScreenMixin {
	@Accessor("searchBox")
	EditBox controlify$getSearchBox();

	@Invoker("refreshSearchResults")
	void controlify$refreshSearchResults();
}
