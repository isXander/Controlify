package dev.isxander.controlify.contextual;

import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.Map;

public record ContextualState(
		Map<Identifier, Boolean> facts,
		Map<Identifier, ItemInstance> items,
		Map<Identifier, BlockInWorld> blocks,
		Map<Identifier, Entity> entities
) {
}
