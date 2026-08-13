/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.contextual;

import dev.isxander.controlify.api.contextual.ContextualStateSink;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

public final class ContextualStateAccumulator implements ContextualStateSink {
	private final Map<Identifier, Boolean> facts = new LinkedHashMap<>();
	private final Map<Identifier, ItemInstance> items = new LinkedHashMap<>();
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
	public void contributeItem(Identifier slot, @Nullable ItemInstance item) {
		if (item == null) {
			return;
		}
		this.items.put(Objects.requireNonNull(slot, "slot"), item);
	}

	@Override
	public void contributeBlock(Identifier slot, @Nullable BlockInWorld block) {
		if (block == null) {
			return;
		}
		this.blocks.put(Objects.requireNonNull(slot, "slot"), block);
	}

	@Override
	public void contributeEntity(Identifier slot, @Nullable Entity entity) {
		if (entity == null) {
			return;
		}
		this.entities.put(Objects.requireNonNull(slot, "slot"), entity);
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
