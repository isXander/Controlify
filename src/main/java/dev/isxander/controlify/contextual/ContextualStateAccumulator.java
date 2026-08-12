/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import dev.isxander.controlify.contextual.api.ContextualStateSink;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ContextualStateAccumulator implements ContextualStateSink {
	private final Map<Identifier, Boolean> facts = new LinkedHashMap<>();
	private final Map<Identifier, ItemStack> items = new LinkedHashMap<>();
	private final Map<Identifier, BlockInWorld> blocks = new LinkedHashMap<>();
	private final Map<Identifier, Entity> entities = new LinkedHashMap<>();

	public ContextualStateAccumulator() {
	}

	public ContextualStateAccumulator(ContextualState state) {
		this.facts.putAll(state.facts());
		this.items.putAll(state.items());
		this.blocks.putAll(state.blocks());
		this.entities.putAll(state.entities());
	}

	@Override
	public void contributeFact(Identifier id, boolean value) {
		this.facts.put(Objects.requireNonNull(id, "id"), value);
	}

	@Override
	public void contributeItem(Identifier slot, ItemStack stack) {
		this.items.put(Objects.requireNonNull(slot, "slot"), Objects.requireNonNull(stack, "stack"));
	}

	@Override
	public void contributeBlock(Identifier slot, BlockInWorld block) {
		this.blocks.put(Objects.requireNonNull(slot, "slot"), Objects.requireNonNull(block, "block"));
	}

	@Override
	public void contributeEntity(Identifier slot, Entity entity) {
		this.entities.put(Objects.requireNonNull(slot, "slot"), Objects.requireNonNull(entity, "entity"));
	}

	public ContextualState view() {
		return new ContextualState(
				Collections.unmodifiableMap(this.facts),
				Collections.unmodifiableMap(this.items),
				Collections.unmodifiableMap(this.blocks),
				Collections.unmodifiableMap(this.entities)
		);
	}

	public ContextualState snapshot() {
		return new ContextualState(
				Map.copyOf(this.facts),
				Map.copyOf(this.items),
				Map.copyOf(this.blocks),
				Map.copyOf(this.entities)
		);
	}
}
