package dev.isxander.controlify.contextual.api;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

public interface ContextualStateSink {
	void contributeFact(Identifier id, boolean value);

	default void contributeFact(Identifier id) {
		this.contributeFact(id, true);
	}

	void contributeItem(Identifier slot, ItemStack stack);

	void contributeBlock(Identifier slot, BlockInWorld block);

	void contributeEntity(Identifier slot, Entity entity);
}
