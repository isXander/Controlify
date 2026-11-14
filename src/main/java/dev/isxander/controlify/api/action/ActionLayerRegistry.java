/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.api.action;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Contract;

import java.util.function.Predicate;

/// Registers layers during {@link ActionEvents#REGISTER_LAYERS}.
/// Layer records remain internal; actions reference their identifiers only.
///
/// Layers form an ordered stack. Input is delivered from the top of the stack down,
/// and each active layer's {@link ActionLayerMode} decides what reaches the layers below it.
/// Order is declared relative to other layers, so layers may reference ones registered later.
public interface ActionLayerRegistry {
	/// Starts registering a layer. Nothing is registered until {@link Builder#register()} is called.
	Builder layer(Identifier id);

	interface Builder {
		/// Defaults to {@link ActionLayerMode#OVERLAY}.
		@Contract("_ -> this")
		Builder mode(ActionLayerMode mode);

		/// The predicate evaluated against the current client before controller input is processed. Required.
		@Contract("_ -> this")
		Builder activeWhen(Predicate<Minecraft> isActive);

		/// Declares that this layer is only ever active while another is, such as a specific screen
		/// requiring {@link BuiltinActionLayers#ANY_SCREEN}. The layer is inactive whenever the other is,
		/// whatever its own predicate says. A layer that is never registered is ignored.
		///
		/// Binding conflict detection relies on this: a layer hidden by a blocking layer that
		/// this one requires can never compete with it for an input.
		@Contract("_ -> this")
		Builder requires(Identifier other);

		/// Places this layer above another. A layer that is never registered is ignored.
		@Contract("_ -> this")
		Builder above(Identifier other);

		/// Places this layer below another. A layer that is never registered is ignored.
		@Contract("_ -> this")
		Builder below(Identifier other);

		/// Places this layer above every layer not also placed at the top.
		@Contract("-> this")
		Builder top();

		/// Places this layer below every layer not also placed at the bottom.
		@Contract("-> this")
		Builder bottom();

		/// Registers the layer and returns its id for use in {@link ActionSpecBuilder#layer(Identifier...)}.
		/// Throws if the identifier already exists, no predicate or placement was given,
		/// or layer registration has closed. Contradictory placements throw when registration closes.
		Identifier register();
	}
}
