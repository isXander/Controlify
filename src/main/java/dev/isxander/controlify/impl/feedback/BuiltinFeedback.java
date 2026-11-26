/*
 * Copyright (C) 2026 isXander
 * This file is part of Controlify.
 *
 * SPDX-License-Identifier: LGPL-3.0-or-later
 */
package dev.isxander.controlify.impl.feedback;

import dev.isxander.controlify.api.feedback.FeedbackEvent;
import dev.isxander.controlify.impl.utils.CUtil;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/// Controlify's built-in feedback events.
///
/// Emit sites (mixins) hold no state: they work out the game-derived numbers (explosion
/// power, block destroy time, move vector length, charge progress, impact force) and pass
/// them as params. What each event feels like is entirely data, in
/// `assets/controlify/controller/feedback/`.
public final class BuiltinFeedback {

	/// @param power the explosion's power (its radius); distance is handled by firing it at a position
	public record Explosion(float power) {
	}

	/// @param destroyTime the block's default destroy time
	public record BlockBreak(float destroyTime) {
	}

	/// @param impact 0 to 1, how hard the player hit the water
	public record WaterImpact(float impact) {
	}

	/// @param chargeProgress 0 to 1 while a chargeable item (a crossbow) is charging, 0 once
	///                       it is charged and for items that do not charge
	public record ItemUse(float chargeProgress) {
	}

	/// What the player is doing, for effects that play from [#AMBIENT].
	///
	/// @param speed        horizontal speed in blocks per second, including any vehicle's
	/// @param fallSpeed    downward speed in blocks per second, 0 when not moving down
	/// @param fallDistance how many blocks the player has fallen so far
	/// @param health       0 to 1, of the player's maximum health
	/// @param hunger       0 to 1, how full the food bar is
	/// @param air          0 to 1, how much breath is left
	public record Ambient(float speed, float fallSpeed, float fallDistance, float health, float hunger, float air) {
		private static final float TICKS_PER_SECOND = 20f;

		public static Ambient of(LocalPlayer player) {
			double dx = player.getX() - player.xo;
			double dy = player.getY() - player.yo;
			double dz = player.getZ() - player.zo;
			return new Ambient(
					(float) Mth.length(dx, dz) * TICKS_PER_SECOND,
					(float) Math.max(-dy, 0.0) * TICKS_PER_SECOND,
					(float) player.fallDistance,
					Math.clamp(player.getHealth() / player.getMaxHealth(), 0f, 1f),
					Math.clamp(player.getFoodData().getFoodLevel() / 20f, 0f, 1f),
					Math.clamp(player.getAirSupply() / (float) player.getMaxAirSupply(), 0f, 1f)
			);
		}
	}


	public static final FeedbackEvent<Void> INCOMING_DAMAGE = event("incoming_damage");
	public static final FeedbackEvent<Void> OUTGOING_DAMAGE = event("outgoing_damage");
	public static final FeedbackEvent<Explosion> EXPLOSION = FeedbackEvent.builder(CUtil.rl("explosion"))
			.param("power", Explosion::power)
			.build();
	/// Lightning is felt from much further away than other positioned events.
	public static final FeedbackEvent<Void> LIGHTNING = FeedbackEvent.builder(CUtil.rl("lightning"))
			.falloffRange(128f)
			.build();
	public static final FeedbackEvent<Void> FISHING_BITE = event("fishing_bite");
	public static final FeedbackEvent<Void> ITEM_BREAK = event("item_break");
	public static final FeedbackEvent<Void> ANVIL = event("anvil");
	public static final FeedbackEvent<Void> DRAGON_DEATH = event("dragon_death");
	public static final FeedbackEvent<Void> WITHER_SPAWN = event("wither_spawn");
	public static final FeedbackEvent<WaterImpact> WATER_LAND = FeedbackEvent.builder(CUtil.rl("water_land"))
			.param("impact", WaterImpact::impact)
			.build();
	public static final FeedbackEvent<BlockBreak> BLOCK_BREAK = FeedbackEvent.builder(CUtil.rl("block_break"))
			.param("destroy_time", BlockBreak::destroyTime)
			.build();
	/// Which effect plays is chosen by feedback rules, by the animation of the item in use.
	public static final FeedbackEvent<ItemUse> USE_ITEM = FeedbackEvent.builder(CUtil.rl("use_item"))
			.param("charge_progress", ItemUse::chargeProgress)
			.build();
	/// Moving focus in a menu. Only felt on controllers with HD haptics.
	public static final FeedbackEvent<Void> UI_NAVIGATE = event("ui_navigate");

	/// Held for as long as the player is in a world. It has no effect of its own: feedback
	/// rules choose what it plays, so a resource pack can add feedback for any contextual
	/// state without code. A rule that should always apply uses `"for": "controlify:ambient"`.
	public static final FeedbackEvent<Ambient> AMBIENT = FeedbackEvent.builder(CUtil.rl("ambient"))
			.param("speed", Ambient::speed)
			.param("fall_speed", Ambient::fallSpeed)
			.param("fall_distance", Ambient::fallDistance)
			.param("health", Ambient::health)
			.param("hunger", Ambient::hunger)
			.param("air", Ambient::air)
			.build();

	/// The effect the vibration test button in the settings plays. It has no event.
	public static final Identifier TEST_VIBRATION = CUtil.rl("test_vibration");

	private static FeedbackEvent<Void> event(String path) {
		return FeedbackEvent.builder(CUtil.rl(path)).build();
	}

	static void register(FeedbackRegistryImpl registry) {
		for (FeedbackEvent<?> event : new FeedbackEvent<?>[] {
				INCOMING_DAMAGE, OUTGOING_DAMAGE, EXPLOSION, LIGHTNING, FISHING_BITE, ITEM_BREAK, ANVIL,
				DRAGON_DEATH, WITHER_SPAWN, WATER_LAND, BLOCK_BREAK, USE_ITEM, UI_NAVIGATE, AMBIENT
		}) {
			registry.registerEvent(event);
		}
	}

	private BuiltinFeedback() {
	}
}
