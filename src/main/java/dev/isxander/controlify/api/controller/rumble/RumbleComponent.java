package dev.isxander.controlify.api.controller.rumble;

import net.minecraft.resources.Identifier;

public interface RumbleComponent {
	/// Queues independent playback of a reusable effect definition for the next rumble tick.
	/// Stopping the returned handle affects only this playback.
	/// The effect ID identifies the event causing the rumble, allowing resource packs
	/// to assign its rumble source independently of the waveform.
	RumbleEffectInstance playEffect(Identifier effectId, RumbleEffect effect);
}
