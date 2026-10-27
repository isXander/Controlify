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

	public static final ActivatorConfigType<ContinuousActivator, ContinuousActivator.Config> CONTINUOUS =
			new ActivatorConfigType<>(ContinuousActivator.Config.TYPE_ID, ContinuousActivator.Config.INSTANCE);

	public static final ActivatorConfigType<HoldLatchActivator, HoldLatchActivator.Config> HOLD_LATCH =
			new ActivatorConfigType<>(HoldLatchActivator.Config.TYPE_ID, HoldLatchActivator.Config.INSTANCE);

	public static final ActivatorConfigType<PressPulseActivator, PressPulseActivator.Config> PRESS_PULSE =
			new ActivatorConfigType<>(PressPulseActivator.Config.TYPE_ID, PressPulseActivator.Config.INSTANCE);

	public static final ActivatorConfigType<ReleasePulseActivator, ReleasePulseActivator.Config> RELEASE_PULSE =
			new ActivatorConfigType<>(ReleasePulseActivator.Config.TYPE_ID, ReleasePulseActivator.Config.INSTANCE);

	public static final ActivatorConfigType<RepeatLatchPulseActivator, RepeatLatchPulseActivator.Config> REPEAT_LATCH_PULSE =
			new ActivatorConfigType<>(RepeatLatchPulseActivator.Config.TYPE_ID, RepeatLatchPulseActivator.Config.MAP_CODEC);

	public static final ActivatorConfigType<TapPulseActivator, TapPulseActivator.Config> TAP_PULSE =
			new ActivatorConfigType<>(TapPulseActivator.Config.TYPE_ID, TapPulseActivator.Config.INSTANCE);

	public static final ActivatorConfigType<DoubleTapPulseActivator, DoubleTapPulseActivator.Config> DOUBLE_TAP_PULSE =
			new ActivatorConfigType<>(DoubleTapPulseActivator.Config.TYPE_ID, DoubleTapPulseActivator.Config.INSTANCE);

	public static final ActivatorConfigType<GuiPressPulseActivator, GuiPressPulseActivator.Config> GUI_PRESS_PULSE =
			new ActivatorConfigType<>(GuiPressPulseActivator.Config.TYPE_ID, GuiPressPulseActivator.Config.INSTANCE);

	public static final ActivatorConfigType<HoldPulseActivator, HoldPulseActivator.Config> HOLD_PULSE =
			new ActivatorConfigType<>(HoldPulseActivator.Config.TYPE_ID, HoldPulseActivator.Config.INSTANCE);

	public static final ActivatorConfigType<ToggleLatchActivator, ToggleLatchActivator.Config> TOGGLE_LATCH =
			new ActivatorConfigType<>(ToggleLatchActivator.Config.TYPE_ID, ToggleLatchActivator.Config.MAP_CODEC);

	public static final ActivatorConfigType<?, ?>[] TYPES = new ActivatorConfigType[]{
			COMPOUND, CONTINUOUS, HOLD_LATCH, PRESS_PULSE, RELEASE_PULSE,
			REPEAT_LATCH_PULSE, TAP_PULSE, DOUBLE_TAP_PULSE, GUI_PRESS_PULSE,
			HOLD_PULSE, TOGGLE_LATCH
	};

	@Override
	public @NotNull String getSerializedName() {
		return this.id();
	}
}
