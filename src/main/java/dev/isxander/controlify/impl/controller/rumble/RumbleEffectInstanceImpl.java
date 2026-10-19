package dev.isxander.controlify.impl.controller.rumble;

import dev.isxander.controlify.api.controller.rumble.ContinuousRumbleEffect;
import dev.isxander.controlify.api.controller.rumble.FiniteRumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleEffect;
import dev.isxander.controlify.api.controller.rumble.RumbleEffectInstance;
import dev.isxander.controlify.api.controller.rumble.RumbleState;
import net.minecraft.resources.Identifier;

import java.util.Objects;

/// Playback state belongs to the handle, never to the reusable definition.
public final class RumbleEffectInstanceImpl implements RumbleEffectInstance {
	private final Identifier effectId;
	private final RumbleEffect effect;
	private int tick, inactivity;
	private boolean started, finished, stopRequested;
	private RumbleState state = RumbleState.NONE;

	public RumbleEffectInstanceImpl(Identifier effectId, RumbleEffect effect) {
		this.effectId = Objects.requireNonNull(effectId);
		this.effect = Objects.requireNonNull(effect);
	}

	public Identifier effectId() {
		return effectId;
	}

	@Override
	public boolean hasStarted() {
		return this.started || this.finished;
	}

	@Override
	public boolean hasFinished() {
		return this.finished;
	}

	public RumbleState currentState() {
		return this.state;
	}

	@Override
	public boolean stop() {
		if (this.finished) {
			return false;
		}
		this.finished = true;
		this.state = RumbleState.NONE;
		return true;
	}

	@Override
	public boolean keepAlive() {
		if (this.finished || !(this.effect instanceof ContinuousRumbleEffect)) {
			return false;
		}
		this.inactivity = 0;
		return true;
	}

	public void tick() {
		if (this.finished) {
			return;
		}
		this.stopRequested |= this.effect.shouldStop();
		switch (this.effect) {
			case FiniteRumbleEffect finite -> {
				if (this.stopRequested || this.tick >= finite.durationTicks()) {
					this.stop();
					return;
				}
			}
			case ContinuousRumbleEffect continuous -> {
				if (this.tick >= continuous.minimumTicks() && (stopRequested || continuous.timeoutTicks() > 0 && inactivity >= continuous.timeoutTicks())) {
					this.stop();
					return;
				}
			}
		}

		this.state = Objects.requireNonNull(effect.stateAt(tick));
		this.started = true;
		if (this.tick < Integer.MAX_VALUE) {
			tick++;
		}
		if (this.inactivity < Integer.MAX_VALUE) {
			this.inactivity++;
		}

		if (this.effect instanceof FiniteRumbleEffect finite && this.tick >= finite.durationTicks())
			this.finished = true;
		if (this.effect instanceof ContinuousRumbleEffect continuous && this.tick >= continuous.minimumTicks()
				&& (this.stopRequested || continuous.timeoutTicks() > 0 && this.inactivity >= continuous.timeoutTicks())) {
			this.finished = true;
		}
	}
}
