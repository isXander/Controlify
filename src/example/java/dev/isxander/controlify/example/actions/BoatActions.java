/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.example.actions;

import dev.isxander.controlify.api.ControlifyApi;
import dev.isxander.controlify.api.action.ActionAccessor;
import dev.isxander.controlify.api.action.ActionEvents;
import dev.isxander.controlify.api.action.ActionLayerMode;
import dev.isxander.controlify.api.action.ActionSpec;
import dev.isxander.controlify.api.action.BuiltinActionLayers;
import dev.isxander.controlify.api.controller.Controller;
import dev.isxander.controlify.api.gui.font.ActionGlyphs;
import dev.isxander.controlify.example.Boats;
import dev.isxander.controlify.example.feedback.BoatFeedback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;

import static dev.isxander.controlify.example.ExampleMod.id;

/// Three actions that only exist while the player is driving a boat:
/// a horn (pulse), a boost (latch) and a throttle (continuous).
public final class BoatActions {
	// #region layer-id
	public static final Identifier BOATING = id("boating");
	// #endregion layer-id

	public static final Component CATEGORY = Component.translatable("examplemod.category.boating");

	// #region accessors
	public static ActionAccessor HORN;
	public static ActionAccessor BOOST;
	public static ActionAccessor THROTTLE;
	// #endregion accessors

	private static boolean wasBoating;

	public static void registerEvents() {
		// #region register-layer
		ActionEvents.REGISTER_LAYERS.subscribeForever(layers -> layers.layer(BOATING)
				.mode(ActionLayerMode.OVERLAY)
				.activeWhen(minecraft -> minecraft.player != null
						&& minecraft.player.getVehicle() instanceof AbstractBoat)
				.requires(BuiltinActionLayers.IN_GAME)
				.above(BuiltinActionLayers.IN_GAME)
				.below(BuiltinActionLayers.ANY_SCREEN)
				.register());
		// #endregion register-layer

		// #region register-actions
		ActionEvents.REGISTER_ACTIONS.subscribeForever(registry -> {
			HORN = registry.register(ActionSpec.builder()
					.id(id("horn"))
					.category(CATEGORY)
					.layer(BOATING)
					.build());
			BOOST = registry.register(ActionSpec.builder()
					.id(id("boost"))
					.category(CATEGORY)
					.layer(BOATING)
					.build());
			THROTTLE = registry.register(ActionSpec.builder()
					.id(id("throttle"))
					.category(CATEGORY)
					.layer(BOATING)
					.build());
		});
		// #endregion register-actions
	}

	// #region read
	public static void tick(Minecraft minecraft) {
		if (minecraft.player == null || !(minecraft.player.getVehicle() instanceof AbstractBoat boat)) {
			return;
		}
		Controller controller = ControlifyApi.get().getCurrentController().orElse(null);
		if (controller == null) {
			return;
		}

		if (BOOST.on(controller).latch().isLatchActive()) {
			Boats.boost(boat);
		}

		// #region feedback
		if (HORN.on(controller).pulse().consumeAllPulses()) {
			Boats.honk(minecraft);
			BoatFeedback.HORN.fire();
		}

		float throttle = THROTTLE.on(controller).continuous().getContinuous();
		if (throttle > 0f) {
			Boats.accelerate(boat, throttle);
			BoatFeedback.ENGINE.hold(new BoatFeedback.Engine(throttle));
		}
		// #endregion feedback
	}
	// #endregion read

	// #region hint
	/// Shows the horn's button when the player boards a boat, if they're playing with a controller.
	public static void showHintOnBoarding(Minecraft minecraft) {
		boolean boating = minecraft.player != null && minecraft.player.getVehicle() instanceof AbstractBoat;
		if (boating && !wasBoating && ControlifyApi.get().currentInputMode().isController()) {
			// #region glyph
			Component hint = Component.translatable("examplemod.hint.horn", ActionGlyphs.action(HORN));
			minecraft.player.sendOverlayMessage(hint);
			// #endregion glyph
		}
		wasBoating = boating;
	}
	// #endregion hint

	private BoatActions() {
	}
}
