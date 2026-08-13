package dev.isxander.controlify.api.contextual;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

public interface ContextualStateSink {
	void contributeFact(Identifier id, boolean value);

	default void contributeFact(Identifier id) {
		this.contributeFact(id, true);
	}

	void contributeItem(Identifier slot, @Nullable ItemInstance stack);

	void contributeBlock(Identifier slot, @Nullable BlockInWorld block);

	void contributeEntity(Identifier slot, @Nullable Entity entity);
}
