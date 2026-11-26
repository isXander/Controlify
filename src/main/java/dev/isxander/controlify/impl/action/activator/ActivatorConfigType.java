/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.action.activator;

import com.mojang.serialization.MapCodec;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public record ActivatorConfigType<A extends Activator, C extends ActivatorConfig<A>>(
		String id,
		MapCodec<C> mapCodec,
		Optional<C> unitConfig
) implements StringRepresentable {
	public ActivatorConfigType(String id, MapCodec<C> mapCodec) {
		this(id, mapCodec, Optional.empty());
	}

	public ActivatorConfigType(String id, C unitConfig) {
		this(id, MapCodec.unit(unitConfig), Optional.of(unitConfig));
	}

	public static final ActivatorConfigType<CompoundActivator, CompoundActivator.Config> COMPOUND =
			new ActivatorConfigType<>(CompoundActivator.Config.TYPE_ID, CompoundActivator.Config.MAP_CODEC);

	public static final ActivatorConfigType<LeafSlot, PulseLeafConfig> PULSE =
			new ActivatorConfigType<>(PulseLeafConfig.TYPE_ID, PulseLeafConfig.MAP_CODEC, Optional.of(PulseLeafConfig.DEFAULT));

	public static final ActivatorConfigType<LeafSlot, SpanLeafConfig> SPAN =
			new ActivatorConfigType<>(SpanLeafConfig.TYPE_ID, SpanLeafConfig.MAP_CODEC, Optional.of(SpanLeafConfig.DEFAULT));

	public static final ActivatorConfigType<LeafSlot, ContinuousLeafConfig> CONTINUOUS =
			new ActivatorConfigType<>(ContinuousLeafConfig.TYPE_ID, ContinuousLeafConfig.INSTANCE);

	public static final ActivatorConfigType<RepeatLatchPulseActivator, RepeatLatchPulseActivator.Config> REPEAT_LATCH_PULSE =
			new ActivatorConfigType<>(RepeatLatchPulseActivator.Config.TYPE_ID, RepeatLatchPulseActivator.Config.MAP_CODEC);

	public static final ActivatorConfigType<ToggleLatchActivator, ToggleLatchActivator.Config> TOGGLE_LATCH =
			new ActivatorConfigType<>(ToggleLatchActivator.Config.TYPE_ID, ToggleLatchActivator.Config.MAP_CODEC);

	public static final ActivatorConfigType<?, ?>[] TYPES = new ActivatorConfigType[]{
			COMPOUND, PULSE, SPAN, CONTINUOUS, REPEAT_LATCH_PULSE, TOGGLE_LATCH
	};

	@Override
	public @NotNull String getSerializedName() {
		return this.id();
	}
}
